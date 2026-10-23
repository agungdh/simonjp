# simonjp

REST API built with Quarkus. Modul yang sudah tersedia: master **bidang**, master
**pangkat golongan**, dan **pegawai** (CRUD penuh, belum termasuk user/auth).

## Stack

- Quarkus 3.40.1.1, Java 25, Gradle Kotlin DSL
- REST: `quarkus-rest` + `quarkus-rest-jackson`
- Validation: `quarkus-hibernate-validator`
- OpenAPI: `quarkus-smallrye-openapi` + `quarkus-swagger-ui`
- Persistence: Hibernate ORM (JPA), PostgreSQL, Flyway
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

Flyway reads migrations from `src/main/resources/db/migration`. `V1` membuat tabel `bidang`,
`pangkat_golongan`, dan `pegawai`; `V2` mengisi seed bidang dan pangkat golongan.
Hibernate uses `schema-management.strategy=validate`, so the mapping must match the migrated
schema. Add schema changes as versioned migrations (`V1__...sql`, `V2__...sql`, ...); never let
Hibernate create or update the schema.

## API documentation

The OpenAPI document is generated from JAX-RS and MicroProfile OpenAPI annotations.

| URL | Description |
| --- | --- |
| <http://localhost:8080/q/swagger-ui> | Interactive Swagger UI |
| <http://localhost:8080/q/openapi> | OpenAPI document (YAML; `?format=json` for JSON) |

Semua endpoint list memakai cursor pagination (`cursor`, `limit`), dan identitas publik memakai
`uuid`.

| Method | Path | Keterangan |
| --- | --- | --- |
| `GET` | `/bidang` | List bidang, filter `search` |
| `POST` | `/bidang` | Buat bidang |
| `GET` | `/bidang/{uuid}` | Detail bidang |
| `PUT` | `/bidang/{uuid}` | Ganti seluruh data bidang |
| `DELETE` | `/bidang/{uuid}` | Soft delete bidang |
| `GET` | `/pangkat-golongan` | List pangkat golongan, filter `search` |
| `POST` | `/pangkat-golongan` | Buat pangkat golongan |
| `GET` | `/pangkat-golongan/{uuid}` | Detail pangkat golongan |
| `PUT` | `/pangkat-golongan/{uuid}` | Ganti seluruh data pangkat golongan |
| `DELETE` | `/pangkat-golongan/{uuid}` | Soft delete pangkat golongan |
| `GET` | `/pegawai` | List pegawai, filter `search`, `tipe`, `status`, `bidangUuid`, `pangkatGolonganUuid`, `kategoriJabatan`, `kategoriKebutuhanJamPelatihan` |
| `POST` | `/pegawai` | Buat pegawai |
| `GET` | `/pegawai/{uuid}` | Detail pegawai |
| `PUT` | `/pegawai/{uuid}` | Ganti seluruh data pegawai |
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
