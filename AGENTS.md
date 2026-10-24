# AGENTS.md

Working instructions for coding agents. `README.md` is the documentation for humans; this file
focuses on what you need when changing code.

> **Documentation language:** `README.md` and `AGENTS.md` must always be written in English. Code
> comments and endpoint naming may follow the mixed Indonesian–English style already present in
> the surrounding files.

## Stack

- Spring Boot 4.1.2 (Gradle plugin), Java 25, Gradle Kotlin DSL (`build.gradle.kts`).
- REST: Spring MVC (`spring-boot-starter-webmvc`); OpenAPI: `springdoc-openapi-starter-webmvc-ui`.
- Persistence: Spring Data JPA / Hibernate, PostgreSQL, Flyway.
- Auth: Spring Security (opaque token), Valkey/Redis for token storage (`spring-boot-starter-data-redis`).
- Lombok (`compileOnly`) + MapStruct, Spring beans (constructor injection).
- Native image: Liberica NIK via `org.graalvm.buildtools.native` (`./gradlew nativeCompile`);
  CI targets `x86-64-v2` (amd64) and `armv8-a` (arm64) via `--build-args="-march=..."`.

## Commands

```shell
docker compose up -d postgres valkey  # required before tests / dev
./gradlew bootRun                # dev mode, http://localhost:8080
./gradlew test                   # requires a running Postgres and Valkey
./gradlew build                  # build + test
./gradlew nativeCompile          # native (Liberica NIK)
```

`src/native-test` holds separate integration tests (`*IT`), run only during native builds.

## Configuration

- `src/main/resources/application.yml` holds runtime config; virtual threads are enabled via
  `spring.threads.virtual.enabled=true`.
- Persistence (when added) must configure `spring.datasource.*` for PostgreSQL, set
  `spring.jpa.hibernate.ddl-auto=validate`, and enable Flyway (`spring.flyway.enabled=true`);
  schema changes go through Flyway migrations, never auto-DDL.
- Redis connection comes from `spring.data.redis.*` (`REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`).
- Auth is configured under `app.auth.*`: cookie name/secure/same-site, sliding TTLs, BCrypt cost,
  and optional admin bootstrap. CORS origin comes from `app.cors.allowed-origins`.
- Do not commit secrets; use environment variables / Spring profiles for environment-specific values.

## Structure & architecture

A simple layered architecture with one-way dependencies:

```text
controller/ → service/ → repository/ → domain/
```

All production code lives in `src/main/java/id/my/agungdh/`:

| Package | Contents | May depend on |
| --- | --- | --- |
| `controller/` | Spring MVC controllers, HTTP + DTO only | `service/`, `dto/` |
| `service/` | Business logic, transactions, resolving `*Uuid` to entities | `repository/`, `domain/`, `dto/`, `mapper/` |
| `repository/` | Database access (Hibernate/`EntityManager`), queries | `domain/` |
| `domain/` | JPA entities. New entities extend `BaseEntity` (identity + audit). Mutable, uses Lombok. | no other application layer |
| `dto/` | Immutable `record` request/response types, camelCase | no other application layer |
| `mapper/` | MapStruct interfaces (`componentModel = "spring"`) | `domain/`, `dto/` |
| `security/` | Filter, principal, token extractor, cookie factory, 401/403 handlers | `repository/`, `domain/`, `dto/`, `config/` |
| `config/` | Spring configuration, properties, OpenAPI/error annotations | all layers |

Layer rules:

1. Controllers only handle HTTP, input validation, and DTOs; never access `EntityManager` or
   repositories directly.
2. Services own business logic and transaction boundaries (`@Transactional`); they may work with
   entities, but anything returned to a controller must be a DTO.
3. Repositories contain SQL/JPA only and return entities, not DTOs; never import `controller/` or
   `dto/`.
4. Domain must not import `service/`, `repository/`, `controller/`, or `dto/`.
5. Do not add hexagonal/clean-architecture ports, adapters, or use-case interfaces; this simple
   layering is enough unless there is a real need.

## Virtual threads, Java 25 & mapping

- **Virtual threads are enabled globally** via `spring.threads.virtual.enabled=true` in
  `application.yml`. Endpoints stay blocking and return DTOs/`Response`. Do not use reactive
  types (`Mono`/`Flux`).
- **Virtual threads suit CRUD** because the workload is mostly blocking I/O (JDBC, Hibernate, HTTP
  clients). This is not a CPU speedup: do not run PDF/Excel generation, compression, or heavy
  computation in this service. Delegate it to another service over an HTTP client; the I/O call
  remains safe on a virtual thread.
- Virtual threads do not add database capacity; still size the connection pool.
- `@Transactional` stays in the service layer, never in controllers.
- **DTOs are always immutable `record`s**, including requests, responses, and pagination wrappers.
  Do not use Lombok/`@Data` in DTOs. Fields are camelCase and annotated with `@Schema` for OpenAPI.
- **Entity ↔ DTO conversion goes through MapStruct only**, in `mapper/` (`componentModel = "spring"`).
  Controllers must not map manually; services use the mappers. When mapping request → entity, `id`,
  `uuid`, and audit columns must be `ignore`d; `uuid`/audit are filled by the service/listener.
- **Java 25 is the target.** Do not lower `sourceCompatibility`/`targetCompatibility`; use `record`,
  pattern matching, and other stable Java 25 features.

## Authentication & authorization

The API uses **opaque tokens**, not JWT and not `HttpSession`. A token is 32 random bytes
(base64url); Valkey stores only `auth:sess:<sha256(token)>` → `userId`, so the raw token is never
persisted and the store never contains user data.

- **Web uses a cookie** (`POST /auth/login`): `HttpOnly`, host-only (no `Domain`), `SameSite=Lax`,
  `Secure` in production, `__Host-` prefix in production. The token never appears in the response
  body.
- **Mobile uses a bearer token** (`POST /auth/token`): the same token and store, returned in the body
  and sent as `Authorization: Bearer <token>`.
- `SessionCreationPolicy.STATELESS` is intentional: there is no `JSESSIONID`, no `HttpSession`, and no
  `spring-session`. Do not call `request.getSession()`, do not store state in the session, and do not
  add `@SessionAttributes`.
- User data is **looked up from PostgreSQL on every request** (soft-delete state, `role`, linked
  pegawai). Never put the principal in the token or in a cookie; a soft-deleted account or a changed
  role must take effect on the next request.
- **A user account is always 1-1 with a pegawai.** `user_account.pegawai_id` is `NOT NULL`, and the
  unique partial index `ux_user_account_pegawai_id_active` allows at most one active account per
  pegawai. The account table stores only `username`, `password_hash`, `role`, and the FK: there is
  no `nama` (read it from the pegawai) and no `enabled` flag (soft delete via `deleted_at` replaces
  it). The FK lives on `user_account` as `pegawai_id`; do not add `user_account_id` to `pegawai`.
- **Deleting a pegawai soft-deletes its account and revokes its tokens.** Soft-deleting the pegawai
  row alone is not enough, because clients could keep using the account; `PegawaiService.delete`
  calls `UserAccountService.softDeleteByPegawai`, which sets `deleted_at`/`deleted_by` and calls
  `AuthTokenRepository.revokeAll`. Deleting the account on its own is not part of the API; account
  management follows the pegawai CRUD.
- **`/auth/login`, `/auth/token`, and `/auth/me` return the pegawai with its bidang.** `UserResponse`
  embeds `UserPegawaiResponse` (`uuid`, `nama`, `nip`, `email`, `bidang`), and `UserMapper` loads the
  relation through MapStruct (`BidangMapper` for the nested bidang). Because `open-in-view` is
  disabled, `AuthService.me()` re-reads the user with the `pegawai` and `pegawai.bidang` entity graph
  instead of reading lazy relations from the principal. The `/pegawai` endpoints keep the leaner
  `PegawaiResponse` with `bidangUuid`.
- The bootstrap admin is also a pegawai: `AuthBootstrap` creates a pegawai row (name from
  `APP_ADMIN_NAMA`, `nip`/`email` left `NULL`) and then the account, in one transaction. There is no
  account without a pegawai, including system/bootstrap accounts.
- TTL is **sliding**: web 8h, mobile 30d. `AuthTokenRepository.refresh` extends the TTL only when the
  remaining lifetime is below 50%, to avoid a Valkey write per request. For cookie sessions the
  server must also re-send `Set-Cookie` with a fresh `Max-Age` whenever it extends the TTL.
- **CSRF is required for cookie requests** and skipped for bearer requests and `POST /auth/token`
  (`BearerAwareCsrfMatcher`). The frontend gets the token from `GET /auth/csrf` (body, not cookie),
  keeps it in memory, and sends it as `X-XSRF-TOKEN`.
- Never log tokens or cookies, and never accept `role`/audit fields from a request body.
- `CurrentUser` exposes the authenticated principal for audit (`@CreatedBy`/`@LastModifiedBy` via
  `JpaAuditConfig`) and for `GET /auth/me`. The principal only carries `id`, `uuid`, `username`, and
  `role`; anything else is re-read from the database when needed.
- 401/403 must keep the `ErrorResponse` shape: filter-chain failures are handled by
  `security/RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, and `CsrfAccessDeniedHandler`;
  failures from `@PreAuthorize` are handled by `GlobalExceptionHandler`.
- Login rate limiting is not implemented yet; add it in `AuthService` when needed.

## Data model & database conventions

New entities share one base: `domain/BaseEntity` (`@MappedSuperclass`), which holds identity and
audit fields. The goal: the `id` bigint stays as a fast internal PK for joins, while the API only
knows about `uuid`.

| Column | PostgreSQL type | Java | Rule |
| --- | --- | --- | --- |
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | `Long` | Internal PK, `@GeneratedValue(strategy = IDENTITY)`, never included in DTOs |
| `uuid` | `UUID` | `UUID` | Public v4 identity (`UUID.randomUUID()`), immutable, generated before insert |
| `created_at` | `TIMESTAMPTZ` | `OffsetDateTime` | Nullable. Set once on insert; `NULL` for imported rows with unknown origin |
| `created_by` | `BIGINT` | `Long` | Nullable. Internal user id, never from the request; `NULL` when unknown |
| `updated_at` | `TIMESTAMPTZ` | `OffsetDateTime` | Nullable. Set on insert and on every update; `NULL` when unknown |
| `updated_by` | `BIGINT` | `Long` | Nullable. Internal user id, never from the request; `NULL` when unknown |
| `deleted_at` | `TIMESTAMPTZ` | `OffsetDateTime` | Nullable. `NULL` = active, set = soft deleted |
| `deleted_by` | `BIGINT` | `Long` | Nullable. Id of the user who deleted the row, `NULL` when unknown |

`id` is guaranteed unique by the database; `uuid` is only unique in practice. The `uuid` index uses
hash and is **not** unique; do not add a unique constraint on `uuid` unless there is an explicit
need. A hash index only serves equality lookups (`WHERE uuid = ?`), not ranges or `ORDER BY`.

All audit columns (`created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`,
`deleted_by`) are internal and **never** appear in DTOs, just like `id`. They are also optional and
informational: they exist only for tracing when inspecting the database directly, so rows imported
from legacy applications may leave any of them `NULL`, and no business logic may depend on them.
The only exception is the soft-delete filter, which uses `deleted_at`; `NULL` there means active.
Clients only need to know `uuid` and domain fields; if timestamps are ever required by the API, that
is an explicit decision that must be documented, not the default.

`BaseEntity` also installs a global filter so deleted rows are not queried:

```java
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity {
    // id, uuid, and audit columns
}
```

Hibernate 7 applies `@SQLRestriction` from a `@MappedSuperclass` to every subclass and qualifies
columns with the table alias, so Hibernate queries (including joins) automatically add
`deleted_at IS NULL`. This only applies to Hibernate queries; native queries must write the
predicate themselves. `@SQLRestriction` is always active and cannot be parameterized; for
admin/restore needs, use a dedicated query instead of removing the global filter. A service that
receives the `uuid` of a deleted row must still treat it as not found (404).

### Reference DDL

```sql
CREATE TABLE contoh (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nama        VARCHAR(255) NOT NULL,
    induk_id    BIGINT REFERENCES contoh(id),

    -- Audit is optional/informational; no NOT NULL and no defaults, so NULL means "unknown".
    created_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_at  TIMESTAMPTZ,
    updated_by  BIGINT,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

-- Public lookup: equality only, no unique.
CREATE INDEX idx_contoh_uuid ON contoh USING hash (uuid);

-- PostgreSQL does not index FKs automatically.
CREATE INDEX idx_contoh_induk_id ON contoh (induk_id);

-- Unique only for active rows, so values can be reused after a soft delete.
CREATE UNIQUE INDEX ux_contoh_nama_active ON contoh (lower(nama)) WHERE deleted_at IS NULL;
```

### Naming

- **Tables: singular**, snake_case: `order_item`, `user_role`. Do not use plural forms (`users`,
  `order_items`); table names follow the entity name.
- Avoid table names that clash with SQL reserved words; use a more specific domain name
  (`purchase_order`, not `order`).
- **Columns: snake_case**: `induk_id`, `nomor_telepon`, `created_at`.
- **Entities: PascalCase singular** (`OrderItem`) with an explicit `@Table(name = "order_item")`.
- **DTOs/JSON: camelCase** (`indukUuid`, `nomorTelepon`).
- **Indexes/constraints**: `pk_<table>`, `fk_<table>_<column>`, `ux_<table>_<column>`,
  `idx_<table>_<column>`; unique indexes that only apply to active rows use the `_active` suffix.

### Derived rules

1. **Never leak `id`.** Response DTOs use `uuid`, requests use `*Uuid` in camelCase (e.g.
   `indukUuid`), and path parameters use `uuid` (e.g. `GET /order-item/{uuid}`). The `id` field and
   all `*_id` columns are internal only (entity, service, repository, SQL). Do not rely on
   `@JsonIgnore`; the DTO shape itself must not have those fields.
2. **FKs stay `*_id` in the database.** JPA relations use `@ManyToOne` + `@JoinColumn(name = "..._id")`.
   Services resolve `*Uuid` from the request into an entity before saving. Never accept `*_id` from
   the client. A denormalized `*_uuid` DB column may only be added for a performance reason and must
   be kept consistent.
3. **Soft delete, not hard delete.** Delete = set `deleted_at` + `deleted_by`. `BaseEntity` already
   installs `@SQLRestriction("deleted_at IS NULL")`, so Hibernate queries filter active rows
   automatically; native queries must add the predicate themselves. Do not use `DELETE FROM` for
   domain data.
4. **Unique = partial index.** Unique indexes always use `WHERE deleted_at IS NULL` so a unique
   value can be reused after the old row is deleted. For string columns, use an expression index
   on `lower(column)` (both for uniqueness and for any filter/lookup index) so comparisons are
   case-insensitive by default; non-string columns keep the plain column.
5. **Audit is filled by the server when known.** `created_*`/`updated_*`/`deleted_*` are filled in
   one place (an audit listener or `@PrePersist`/`@PreUpdate`) using `OffsetDateTime` (not
   `LocalDateTime`). The `*_by` columns come from the logged-in principal
   (`security/CurrentUser` through the `AuditorAware` bean in `JpaAuditConfig`), never from the
   request body. When there is no principal (seeder, import job), leave `*_by` as `NULL` ("unknown")
   instead of inventing a value such as `0L`.
   All audit columns are nullable because data imported from legacy applications may not carry this
   metadata: `NULL` means "unknown", never invent a value, and do not make behavior depend on audit
   values (except `deleted_at` for the soft-delete filter). All
   audit columns (`*_at` and `*_by`) are never exposed to clients.
6. **MapStruct ignores internal columns.** When mapping request → entity, `id`, `uuid`, and all
   audit columns must be `ignore`d; `uuid`/audit are filled by the service/listener. Subclass
   entities should use `@Getter`/`@Setter` (not `@Data`) so `equals`/`hashCode` do not depend on
   mutable fields.

## DTO & JSON conventions

- DTO field names (record components) and JSON use camelCase: `indukUuid`, `nomorTelepon`.
- Database table and column names stay snake_case: `order_item`, `induk_id`, `nomor_telepon`; do not
  convert them to camelCase. Tables use the singular form (see the naming section).
- Do not add `@JsonProperty`/`@JsonNaming` to force snake_case. Let Jackson use the record names as
  they are.
- Multi-word query parameters use camelCase (`pageSize`, `sortBy`); identity path parameters stay
  `uuid`.
- MapStruct maps automatically when DTO and entity field names match; when they differ, use
  `@Mapping(source = "...", target = "...")`.

## REST conventions

REST does not require plural forms; that is only a style convention. What matters is consistency
and predictability.

- Resource paths use **singular** and kebab-case: `/order-item`, `/order-item/{uuid}`. Do not mix
  singular and plural.
- Actions are determined by the HTTP method, not by verbs in the path: `GET /order-item`,
  `POST /order-item`, `GET /order-item/{uuid}`, `PUT /order-item/{uuid}`, `DELETE /order-item/{uuid}`.
- **Updates use `PUT` with full-replace semantics.** Clients must send every required field; an
  omitted optional field is treated as `null` and clears the old value. Do not use `PATCH`/partial
  updates unless there is an explicit, documented need.
- Identity path parameters are always `uuid`, never `id`.
- **Error responses carry only `message` (plus an optional `fields` map for validation, where each
  key is a field and value is a list of messages).** The HTTP status code is already conveyed by the
  response status line, so do not duplicate it in the body (e.g. `ErrorResponse` has `message` and
  optional `fields`). Validation errors are grouped per field and may contain more than one entry
  per field.

### Swagger / OpenAPI operation order

Declare handler methods in a controller in the same order they must appear in Swagger UI:
**list → create → get one → update → delete**, and put any specific feature endpoint **below
`delete`**.

- Swagger UI groups operations by path, so `GET /order-item` (list) and `POST /order-item` (create)
  share the collection path and always render together; the item operations
  (`GET`/`PUT`/`DELETE /order-item/{uuid}`) follow.
- springdoc orders `paths` by `RequestMappingInfo`, not by declaration order, so
  `config/OpenApiOperationOrderCustomizer` re-sorts the generated spec into the order above.
  Endpoints it does not recognize (specific features) are placed below `delete`. When adding a
  feature endpoint, declare it after `delete`; do not remove the customizer.

## Error handling

- All errors go through `GlobalExceptionHandler` (`@RestControllerAdvice`); controllers do not build
  error bodies manually.
- `ErrorResponse` carries only `message` plus an optional `fields` map. The HTTP status code is
  conveyed by the response status line and is not duplicated in the body.
- For `@Valid` validation failures, `fields` is a map of field name -> list of messages, grouped
  per field, so a single field can report more than one violation. For any other error, `fields` is
  omitted entirely (no `"fields": null`), so the body only contains `message`.
- Controllers annotate request bodies with `@Valid`; services throw `ResponseStatusException` (or
  domain-specific exceptions handled by the advice) for not-found / bad-request cases.
- OpenAPI error documentation uses meta-annotations in `config/`: `@ApiValidationError` (400 with
  body), `@ApiBadRequest` (400 invalid path parameter), `@ApiInvalidCursor` (400 cursor),
  `@ApiNotFound` (404), and `@ApiInternalError` (500). Body examples live in `ApiErrorExamples`;
  do not duplicate JSON examples inside controllers.
- springdoc 3.1.0 stops inferring the success response from the return type as soon as an operation
  has `@ApiResponses`, so every endpoint must also declare its success code (`200`/`201`/`204`) with
  `@ApiResponse` and no `content`; the schema is still resolved from the return type, including
  generic wrappers such as `CursorPageDTO<T>`.
- Validation messages come from the bean validation annotations; do not hand-build per-field
  messages in controllers.

## Pagination (cursor)

**Every** list endpoint uses **cursor pagination**, including small master tables. The goal is a
uniform API contract: one request shape, one response shape, one client implementation. Offset gets
slower with every page and does not suit infinite scroll, so it is not used as the primary path
anywhere.

Accepted consequence: there are no page numbers and no total count. For master tables, navigation
happens through filters/search, not `page=N`. `COUNT(*)` is only added when there is an explicit
need, as a separate endpoint — never tucked into the list response.

- The only cursor exposed to clients is the `uuid` of the last row. Never send `id`, an offset, or a
  page number in the cursor.
- Query params: `cursor` (UUID, optional), `sort`, and `order`
  (`asc`/`desc`). `limit` is fixed at 20 and is not client-configurable.
- When `sort` is not sent, default to `ORDER BY id DESC`; `order` defaults to `desc`. `id` stays
  internal and is not exposed to clients.
- When `cursor` is present, the service:
  1. loads the cursor row by `uuid` (hash index lookup);
  2. if it does not exist or is soft deleted, returns `400` invalid cursor — not `404`;
  3. verifies the cursor row still matches the same filter; if not, `400`;
  4. uses that row's `id` (and sort column value) as the keyset position;
  5. queries the next page with the same filter + keyset predicate + `ORDER BY`.
- A cursor is only valid for the same filter + sort combination. If the client changes
  filter/sort, reset the cursor (send the request without `cursor`).
- Always append `id` as a unique tiebreaker at the end of `ORDER BY`, and use tuple comparison for
  non-unique sorts.
- Fetch `limit + 1` rows to determine `hasMore`, then drop the extra row. `nextCursor` is the `uuid`
  of the last row actually returned, or `null` when there is nothing more.
- Filters must also be applied when looking up the cursor; never use the position of a row that the
  current filter cannot see.
- Because `uuid` is not unique in the database, never use `getSingleResult()` for the cursor
  lookup; fetch one row deterministically (`ORDER BY id LIMIT 1`) or handle duplicates.
- `sort` only accepts whitelisted fields; never interpolate column names from the request into
  JPQL/SQL. Do not make `uuid` the primary sort column, since UUID v4 is random, not time-ordered.
- Do not use offset for any list endpoint. The only exception is an explicit, documented request,
  e.g. a dedicated endpoint that needs "jump to page N".
- Small tables are not exempt from this rule, but they **do not need a keyset index**. A seq scan on
  a master table of a few pages is cheap; a keyset query stays correct without an index.
- `sort` may be unsupported entirely on master tables (the default `ORDER BY id DESC` is enough).
  That is still consistent because `sort` is optional in the contract; do not create per-resource
  parameters.
- Avoid sorting on frequently changing columns (`updated_at`) for long lists: rows can be skipped or
  duplicated when values change mid-pagination. For master tables, sort by a stable column.

Response shape:

```json
{
  "items": [],
  "nextCursor": "0f1e2d3c-...",
  "hasMore": true
}
```

The DTO can be generic:

```java
public record CursorPageDTO<T>(List<T> items, UUID nextCursor, boolean hasMore) {
}
```

Default keyset example (`sort` not sent → `ORDER BY id DESC`):

```sql
SELECT ...
FROM order_item
WHERE /* filter */
  AND (:cursorId IS NULL OR id < :cursorId)
ORDER BY id DESC
LIMIT :limitPlusOne;
-- deleted_at IS NULL is added automatically by @SQLRestriction
```

For non-unique sorts:

```sql
WHERE (sort_col, id) < (:cursorSort, :cursorId)
ORDER BY sort_col DESC, id DESC
```

Supporting indexes:

- cursor lookup: the existing hash index on `uuid`;
- default keyset: the `id` PK; if there are many soft-deleted rows, a partial index
  `(id) WHERE deleted_at IS NULL`;
- other sorts: a btree `(sort_col, id) WHERE deleted_at IS NULL`.

Only add the indexes above to tables that are large enough. Small master tables keep using cursor
pagination without extra keyset indexes — do not create an index just to "stay consistent". A
standalone `deleted_at` index does not help an `IS NULL` predicate (almost every row matches, so the
planner picks a seq scan) and only adds write cost. For querying deleted rows (purge/restore), use a
partial index `(deleted_at) WHERE deleted_at IS NOT NULL`.

## Rules

1. **The schema is managed by Flyway, not Hibernate.** Hibernate uses
   `spring.jpa.hibernate.ddl-auto=validate`. Schema changes must be migrations in
   `src/main/resources/db/migration` (`V1__...sql`, `V2__...sql`, etc.); do not rely on auto-DDL.
   While the project is pre-release, keep one schema migration and fold changes into it instead of
   adding revision migrations (as long as no shared/production database has applied it). Once a
   migration has shipped, treat it as immutable and add a new version instead.
   Do not modify a migration that has already been applied outside local/dev databases.
2. **Entities must not leave the REST layer.** Controllers return DTOs only; internal fields such as
   `id`, all `*_id`, and every audit column (`created_*`, `updated_*`, `deleted_*`) are deliberately
   absent from DTOs. The public identity that clients use is `uuid`/`*Uuid` (see the data model
   section above).
3. **Annotation processor order matters** in `build.gradle.kts`: Lombok → `lombok-mapstruct-binding`
   → MapStruct. Changing this order makes mappers fail to see Lombok-generated getters/builders.
4. **Tests use a real PostgreSQL and Valkey**, not H2/in-memory. Spring Boot tests (`@SpringBootTest`)
   fail unless `docker compose up -d postgres valkey` has been run.
5. **Java 25 + virtual threads:** do not lower compatibility; use `record` for DTOs, enable virtual
   threads via `spring.threads.virtual.enabled=true`, and MapStruct for entity ↔ DTO (details in the
   virtual threads section above).
6. UTF-8 encoding and the `-parameters` compiler flag are already configured in `build.gradle.kts`;
   do not remove them (Jackson/MapStruct need them).
7. Code comments and endpoint names in this repo mix Indonesian and English. Follow the style of the
   surrounding files; do not normalize existing text. `README.md` and `AGENTS.md` themselves are
   always in English.
8. Do not commit `build/`, `.gradle/`, or generated artifacts.
9. **`id` internal, `uuid` public.** Do not expose `id` or `*_id` through DTOs, query params, paths,
   headers, or error messages; use `uuid`/`*Uuid` (`*_uuid` is only a database column name).
10. **Domain data is never physically deleted.** Soft delete via `deleted_at`/`deleted_by`;
    `BaseEntity` installs `@SQLRestriction("deleted_at IS NULL")` so Hibernate queries read only
    active data. New unique indexes must be partial (`WHERE deleted_at IS NULL`).
11. **Audit columns are filled by the server when known**, including
    `created_by`/`updated_by`/`deleted_by` from the principal — never accept them from the request
    body. They are nullable and informational: leave them `NULL` instead of inventing values for
    imported legacy data.
12. **Every list endpoint uses cursor pagination**, including small master tables, so the API
    contract stays uniform. The cursor is the `uuid` of the last row; offsets/pages are not used.
    Small tables may skip keyset indexes and may not support `sort`. Details in the pagination
    section above.
13. **CPU-heavy work (PDF/Excel generation, compression) does not run in this service.** Delegate it
    to another service over an HTTP client; virtual threads are only for blocking I/O CRUD.
14. **Controller methods follow the Swagger order** — list → create → get one → update → delete, with
    specific feature endpoints declared after `delete`. `config/OpenApiOperationOrderCustomizer`
    enforces that order in the generated spec; details in the REST conventions section.
15. **Auth is opaque-token based, cookie for web and bearer for mobile.** Tokens live in Valkey as
    `auth:sess:<sha256(token)>` → `userId`; never store the raw token or user data there, and never
    reintroduce `HttpSession`/`spring-session` (the app is `STATELESS` on purpose). A user account is
    always 1-1 with a pegawai (`pegawai_id NOT NULL`), has no `nama`/`enabled` columns, and is
    soft-deleted together with its pegawai while its tokens are revoked. Details in the
    authentication section above.
16. **Cookie requests require CSRF; bearer requests do not.** The frontend reads the CSRF token from
    the body of `GET /auth/csrf` (cross-subdomain JavaScript cannot read the API cookie) and sends it
    as `X-XSRF-TOKEN`. Do not disable CSRF globally to make Swagger or a client simpler.
