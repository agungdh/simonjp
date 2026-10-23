# AGENTS.md

Instruksi kerja untuk coding agent. `README.md` adalah dokumentasi untuk manusia; file ini fokus
pada hal yang dibutuhkan saat mengubah kode.

## Stack

- Quarkus 3.40.1.1 (BOM di `gradle.properties`), Java 25, Gradle Kotlin DSL (`build.gradle.kts`).
- REST: `quarkus-rest` + `quarkus-rest-jackson`; OpenAPI: `quarkus-smallrye-openapi` + `quarkus-swagger-ui`.
- Persistensi: Hibernate ORM (JPA), PostgreSQL, Flyway.
- Lombok (`compileOnly`) + MapStruct, semuanya CDI bean.

## Perintah

```shell
docker compose up -d postgres     # wajib sebelum test / dev
./gradlew quarkusDev              # dev mode, http://localhost:8080
./gradlew test                    # butuh Postgres hidup
./gradlew build                   # build + test
./gradlew build -Dquarkus.native.enabled=true   # native (GraalVM)
```

`src/native-test` berisi integration test terpisah (`*IT`), dijalankan hanya pada build native.

## Struktur

Semua kode produksi ada di `src/main/java/id/my/agungdh/`:

| Paket | Isi |
| --- | --- |
| `domain/` | Entity JPA (`Salam`, dipetakan ke tabel `salam`). Mutable, memakai Lombok `@Data`/`@Builder`. |
| `dto/` | `record` immutable yang diekspos REST layer (`SalamDTO`, `SalamRequest`, `HelloDTO`). |
| `mapper/` | Interface MapStruct `SalamMapper` (`componentModel = "cdi"`). |
| `service/` | `SalamService`, contoh penyimpanan in-memory. |
| `resource/` | JAX-RS resource (`GreetingResource`, `DataResource`). |

## Aturan

1. **Skema dikelola Flyway, bukan Hibernate.** Hibernate memakai `schema-management.strategy=validate`.
   Perubahan skema harus berupa migrasi baru di `src/main/resources/db/migration`
   (`V2__...sql`), jangan mengubah migrasi yang sudah ada dan jangan mengandalkan auto-DDL.
2. **Entity tidak boleh keluar dari REST layer.** Resource hanya mengembalikan DTO; field internal
   seperti `catatanInternal` sengaja tidak ada di `SalamDTO` maupun `SalamRequest`.
3. **`SalamService` masih in-memory** walaupun `Salam` sudah berupa entity JPA. Jangan berasumsi data
   sudah tersimpan ke database; kalau butuh persistensi, tambahkan repository/`EntityManager` secara
   eksplisit dan sesuaikan test.
4. **Urutan annotation processor penting** di `build.gradle.kts`: Lombok → `lombok-mapstruct-binding`
   → MapStruct. Mengubah urutan ini membuat mapper gagal melihat getter/builder hasil Lombok.
5. **Test memakai PostgreSQL asli**, bukan H2/in-memory. Test Quarkus (`@QuarkusTest`) akan gagal
   kalau `docker compose up -d postgres` belum dijalankan.
6. Java 25 tersedia: pakai `record` untuk DTO dan `@RunOnVirtualThread` untuk endpoint blocking.
7. Encoding UTF-8 dan compiler flag `-parameters` sudah diatur di `build.gradle.kts`; jangan
   menghapusnya (dibutuhkan Jackson/MapStruct).
8. Komentar dan nama endpoint di repo ini campur Indonesia–Inggris. Ikuti gaya di file sekitarnya,
   jangan menyeragamkan yang sudah ada.
9. Jangan commit `build/`, `.gradle/`, atau artefak hasil generate.
