# simonjp

REST API built with Quarkus. The repository currently contains only the project
infrastructure: no domain endpoints or schema migrations yet.

## Stack

- Quarkus 3.40.1.1, Java 25, Gradle Kotlin DSL
- REST: `quarkus-rest` + `quarkus-rest-jackson`
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

To send your first endpoint, add a JAX-RS resource under
`src/main/java/id/my/agungdh/resource/`. See `AGENTS.md` for the layering and naming rules.

## Database

The datasource defaults match `compose.yml` and can be overridden with environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5432/simonjp` |
| `DB_USERNAME` | `admin` |
| `DB_PASSWORD` | `admin` |

Flyway reads migrations from `src/main/resources/db/migration` (currently empty).
Hibernate uses `schema-management.strategy=validate`, so the mapping must match the migrated
schema. Add schema changes as versioned migrations (`V1__...sql`, `V2__...sql`, ...); never let
Hibernate create or update the schema.

## API documentation

The OpenAPI document is generated from JAX-RS and MicroProfile OpenAPI annotations.

| URL | Description |
| --- | --- |
| <http://localhost:8080/q/swagger-ui> | Interactive Swagger UI |
| <http://localhost:8080/q/openapi> | OpenAPI document (YAML; `?format=json` for JSON) |

With no resources defined yet, the document contains only the API info metadata.

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

The directories are created as code is added. Layer dependencies and database conventions are
documented in [`AGENTS.md`](AGENTS.md).
