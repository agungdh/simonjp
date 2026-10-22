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

PDFBox maps the standard 14 fonts to metric-compatible system fonts. The `Dockerfile` copies
Liberation Sans, Serif, and Mono from a CentOS Stream 9 build stage into the UBI 9 minimal
runtime image, so those mappings resolve without fallback-font warnings.

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
