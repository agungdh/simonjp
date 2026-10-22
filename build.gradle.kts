plugins {
    java
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val lombokVersion: String by project
val mapstructVersion: String by project
val lombokMapstructBindingVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-swagger-ui")
    implementation("io.quarkiverse.pdfbox:quarkus-pdfbox:1.2.3")

    // Lombok hanya dipakai saat compile, tidak ikut ke runtime/native image.
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    // Urutan penting: Lombok dulu, lalu binding, baru MapStruct.
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation("io.quarkus:quarkus-junit")
    testImplementation("io.rest-assured:rest-assured")
}

group = "id.my.agungdh"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// PDFBox scans the installed system fonts the first time a standard 14 font is created and
// caches the result in `.pdfbox.cache`. Building that cache during the build keeps the logs of
// a running application free of the scan.
//
// `fontCacheTool` assembles the small builder (our class plus the PDFBox jars) that the Docker
// image build runs against the fonts installed in the runtime image. The builder lives in its
// own source set and targets Java 17, so it can run on the JDK in the Docker build stage.
val fontCacheToolSourceSet = sourceSets.create("fontCacheTool") {
    java.setSrcDirs(listOf("src/fontCacheTool/java"))
    compileClasspath += sourceSets.main.get().compileClasspath
}

tasks.named<JavaCompile>(fontCacheToolSourceSet.compileJavaTaskName) {
    options.release.set(17)
}

val fontCacheTool by tasks.registering(Sync::class) {
    description = "Assembles the PDFBox font-cache builder used by the Docker image build."
    group = "build"
    into(layout.buildDirectory.dir("fontcache-tool"))
    from(fontCacheToolSourceSet.output)
    from(configurations.runtimeClasspath) {
        include("pdfbox-3*.jar", "fontbox-3*.jar", "pdfbox-io-3*.jar", "commons-logging-1*.jar")
    }
}

// Dev mode runs on the local machine, so warm the cache in the default location
// (`~/.pdfbox.cache`) before the application starts.
val warmPdfBoxFontCache by tasks.registering(JavaExec::class) {
    description = "Warms PDFBox's on-disk font cache so dev mode does not scan fonts at runtime."
    group = "application"
    classpath = files(fontCacheToolSourceSet.output, configurations.runtimeClasspath)
    mainClass.set("id.my.agungdh.fontcache.FontCacheBuilder")
}

tasks.named("build") {
    dependsOn(fontCacheTool)
}

tasks.named("quarkusDev") {
    dependsOn(warmPdfBoxFontCache)
}
