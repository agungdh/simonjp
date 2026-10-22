# simonjp

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Generate PDF example

`GET /pdf/contoh` returns a generated PDF (`application/pdf`) that demonstrates:

- Helvetica, Times, and Courier (standard 14 fonts), including bold, italic, and bold-italic
- Underlined text, custom colors, and font sizes from 9 pt to 36 pt
- A JPEG image embedded directly with `DCTDecode` (no `ImageIO` needed)
- Vector bar and line charts drawn with PDF operators

PDF support comes from the Quarkiverse extension
[`io.quarkiverse.pdfbox:quarkus-pdfbox`](https://docs.quarkiverse.io/quarkus-pdfbox/dev/index.html),
which registers the GraalVM reflection and resource configuration for native images and pulls in
`quarkus-awt`.

The sample JPEG is registered for native images with
`quarkus.native.resources.includes=images/**` in `application.yml`. The native `Dockerfile`
also copies the generated `build/*.so` files, which `quarkus-awt` produces.

PDFBox maps the standard 14 fonts to metric-compatible system fonts. The `Dockerfile` installs
Liberation Sans, Serif, and Mono on the Rocky Linux 9 minimal runtime image, so those mappings
resolve without fallback-font warnings.

## Lombok, MapStruct, and record DTOs

The sample API is split into layers so the mapping is easy to see:

| Layer | Class | Purpose |
| --- | --- | --- |
| Entity | `domain/Salam` | Mutable model with Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| DTO | `dto/SalamDTO`, `dto/SalamRequest`, `dto/HelloDTO` | Immutable `record` DTOs used by the REST layer |
| Mapper | `mapper/SalamMapper` | MapStruct mapper with `componentModel = "cdi"`, injectable as a CDI bean |
| Service | `service/SalamService` | In-memory sample data and entity <-> DTO mapping |
| Resource | `resource/DataResource` | REST endpoints that only expose DTOs |

Endpoints:

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/hello` | Returns `HelloDTO`: message, timestamp, thread name, and virtual-thread flag |
| `GET` | `/contoh/data` | Returns the sample data as a list of `SalamDTO` |
| `GET` | `/contoh/data/{id}` | Returns one `SalamDTO`, or `404` when the id is unknown |
| `POST` | `/contoh/data` | Accepts a `SalamRequest`, returns `201` with the created `SalamDTO` |

```shell script
curl -s localhost:8080/hello
# {"message":"Hello from Quarkus REST","timestamp":"...","thread":"VirtualThread[...]","virtualThread":true}

curl -s localhost:8080/contoh/data
# [{"id":1,"nama":"Agung","pesan":"Halo dari data contoh","dibuatPada":"..."}, ...]

curl -s -X POST localhost:8080/contoh/data \
  -H 'Content-Type: application/json' \
  -d '{"nama":"Dewi","pesan":"Halo dari POST"}'
# {"id":4,"nama":"Dewi","pesan":"Halo dari POST","dibuatPada":"..."}
```

`Salam.catatanInternal` exists on the entity but is not part of `SalamDTO`, so it never reaches the
client. `SalamRequest` only contains the fields a client may set; the mapper ignores `id`,
`catatanInternal`, and `dibuatPada` when converting the request to an entity.

Lombok is applied to every hand-written class where it removes boilerplate:

- `domain/Salam` — `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `GreetingResource`, `PdfDemoResource`, `DataResource` — `@JBossLog` for the logger
- `DataResource`, `SalamService`, `ChartPainter` — `@RequiredArgsConstructor` for constructor injection
- `PdfDemoGenerator` — `@NoArgsConstructor(access = AccessLevel.PRIVATE)`
- `SalamService` — `@PostConstruct` seeds the sample data after the generated constructor runs

There is nothing for Lombok to generate in the record DTOs, the MapStruct interface, or the tests,
so those stay plain Java.

Lombok is declared as `compileOnly` and both Lombok and MapStruct run only at compile time
(`annotationProcessor`). The `lombok-mapstruct-binding` processor must run between them so MapStruct
can see Lombok's generated builder/getters. Because no reflection or runtime processing is involved,
these classes work in a native image without extra configuration.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/simonjp-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): Build RESTful web services and APIs using Jakarta REST (formerly JAX-RS)

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
