# simonjp

REST API built with Quarkus. Available modules: **bidang** master data,
**pangkat golongan** master data, and **pegawai** (full CRUD, no user/auth yet).

## Stack

- Quarkus 3.40.1.1, Java 25, Gradle Kotlin DSL
- REST: `quarkus-rest` + `quarkus-rest-jackson`
- Validation: `quarkus-hibernate-validator`
- OpenAPI: `quarkus-smallrye-openapi` + `quarkus-swagger-ui`
- Persistence: Hibernate ORM with Panache, PostgreSQL, Flyway
- Lombok (compile-time) + MapStruct

## Running in dev mode

Start PostgreSQL first; Flyway runs when the application boots.

```shell
docker compose up -d postgres
./gradlew quarkusDev
```

The application is available at <http://localhost:8080>. Dev UI: <http://localhost:8080/q/dev/>.

Domain endpoints are under `src/main/java/id/my/agungdh/resource/`. See `AGENTS.md` for the layering
and naming rules.

## Database

The datasource defaults match `compose.yml` and can be overridden with environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5432/simonjp` |
| `DB_USERNAME` | `admin` |
| `DB_PASSWORD` | `admin` |

Flyway reads migrations from `src/main/resources/db/migration`. `V1` creates the `bidang`,
`pangkat_golongan`, and `pegawai` tables; `V2` seeds bidang and pangkat golongan.
Hibernate uses `schema-management.strategy=validate`, so the mapping must match the migrated
schema. Add schema changes as versioned migrations (`V1__...sql`, `V2__...sql`, ...); never let
Hibernate create or update the schema.

## API documentation

The OpenAPI document is generated from JAX-RS and MicroProfile OpenAPI annotations.

| URL | Description |
| --- | --- |
| <http://localhost:8080/q/swagger-ui> | Interactive Swagger UI |
| <http://localhost:8080/q/openapi> | OpenAPI document (YAML; `?format=json` for JSON) |

Every list endpoint uses cursor pagination (`cursor`, `limit`), and public identities use `uuid`.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/bidang` | List bidang, `search` filter |
| `POST` | `/bidang` | Create bidang |
| `GET` | `/bidang/{uuid}` | Bidang detail |
| `PUT` | `/bidang/{uuid}` | Replace all bidang data |
| `DELETE` | `/bidang/{uuid}` | Soft delete bidang |
| `GET` | `/pangkat-golongan` | List pangkat golongan, `search` filter |
| `POST` | `/pangkat-golongan` | Create pangkat golongan |
| `GET` | `/pangkat-golongan/{uuid}` | Pangkat golongan detail |
| `PUT` | `/pangkat-golongan/{uuid}` | Replace all pangkat golongan data |
| `DELETE` | `/pangkat-golongan/{uuid}` | Soft delete pangkat golongan |
| `GET` | `/pegawai` | List pegawai, filters `search`, `tipe`, `status`, `bidangUuid`, `pangkatGolonganUuid`, `kategoriJabatan`, `kategoriKebutuhanJamPelatihan` |
| `POST` | `/pegawai` | Create pegawai |
| `GET` | `/pegawai/{uuid}` | Pegawai detail |
| `PUT` | `/pegawai/{uuid}` | Replace all pegawai data |
| `DELETE` | `/pegawai/{uuid}` | Soft delete pegawai |

## Build

```shell
./gradlew test                                  # unit/integration tests, needs PostgreSQL
./gradlew build                                 # build + test
./gradlew build -Dquarkus.native.enabled=true   # native executable (GraalVM/Mandrel)
```

The JVM build produces `build/quarkus-app/quarkus-run.jar`. The native build produces
`build/*-runner`. Native integration tests (`*IT`) live in `src/native-test`.

## Project layout

```text
src/main/java/id/my/agungdh/
├── domain/      # JPA entities
├── dto/         # immutable record DTOs
├── mapper/      # MapStruct mappers
├── repository/  # database access
├── resource/    # JAX-RS resources (HTTP only)
└── service/     # business logic and transactions
```

The directories map one-to-one to the layers above. Layer dependencies and database conventions are
documented in [`AGENTS.md`](AGENTS.md).

## Authentication (JWT)

Auth uses Quarkus Security + SmallRye JWT with the token carried in the `SESSION` cookie.
The RSA key pair is **not** committed. Generate it locally before running:

```shell
openssl genpkey -algorithm RSA -out src/main/resources/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/privateKey.pem -out src/main/resources/publicKey.pem
```

Default seeded admin user: `admin` / `admin` (created on first boot if no user exists).
