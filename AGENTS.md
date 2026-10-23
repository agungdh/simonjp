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
| `domain/` | Entity JPA. Entity baru mewarisi `BaseEntity` (identitas + audit); `Salam` masih contoh lama. Mutable, memakai Lombok. |
| `dto/` | `record` immutable yang diekspos REST layer (`SalamDTO`, `SalamRequest`, `HelloDTO`). |
| `mapper/` | Interface MapStruct `SalamMapper` (`componentModel = "cdi"`). |
| `service/` | `SalamService`, contoh penyimpanan in-memory. |
| `resource/` | JAX-RS resource (`GreetingResource`, `DataResource`). |

## Model data & konvensi database

Entity baru memakai satu basis: `domain/BaseEntity` (`@MappedSuperclass`) yang menyimpan identitas
dan audit. Tujuannya: `id` bigint tetap menjadi PK internal yang cepat untuk join, sementara API
hanya mengenal `uuid`.

| Kolom | Tipe PostgreSQL | Java | Aturan |
| --- | --- | --- | --- |
| `id` | `BIGINT GENERATED ALWAYS AS IDENTITY` | `Long` | PK internal, `@GeneratedValue(strategy = IDENTITY)`, tidak pernah masuk DTO |
| `uuid` | `UUID` | `UUID` | identitas publik v4 (`UUID.randomUUID()`), immutable, di-generate sebelum insert |
| `created_at` | `TIMESTAMPTZ` | `OffsetDateTime` | diisi sekali saat insert |
| `created_by` | `BIGINT` | `Long` | id user internal, bukan dari request |
| `updated_at` | `TIMESTAMPTZ` | `OffsetDateTime` | diisi saat insert dan setiap update |
| `updated_by` | `BIGINT` | `Long` | id user internal, bukan dari request |
| `deleted_at` | `TIMESTAMPTZ` | `OffsetDateTime` | `NULL` = aktif, terisi = soft delete |
| `deleted_by` | `BIGINT` | `Long` | id user yang menghapus |

`id` dijamin unik oleh database; `uuid` hanya unik secara praktik. Index `uuid` memakai hash dan
**tidak** unique; jangan menambahkan constraint unik pada `uuid` kecuali ada kebutuhan eksplisit.
Hash index hanya melayani pencarian equality (`WHERE uuid = ?`), bukan range atau `ORDER BY`.

Semua kolom audit (`created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`,
`deleted_by`) bersifat internal dan **tidak pernah** masuk DTO, sama seperti `id`. Client cukup
mengenal `uuid` dan field domain; kalau suatu saat timestamp memang dibutuhkan API, itu keputusan
eksplisit yang harus didokumentasikan, bukan default.

`BaseEntity` juga memasang filter global agar data terhapus tidak ikut ter-query:

```java
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity {
    // id, uuid, dan kolom audit
}
```

Hibernate 7 menerapkan `@SQLRestriction` dari `@MappedSuperclass` ke semua entity turunan dan
meng-qualify kolom dengan alias tabel, jadi query Hibernate (termasuk join) otomatis menambahkan
`deleted_at IS NULL`. Ini hanya berlaku untuk query Hibernate; native query wajib menulis predicate
sendiri. `@SQLRestriction` selalu aktif dan tidak bisa di-parameterisasi; untuk kebutuhan
admin/restore, pakai query khusus, jangan melepas filter global. Service yang menerima `uuid` data
terhapus tetap harus memperlakukannya sebagai tidak ditemukan (404).

### DDL acuan

```sql
CREATE TABLE contoh (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL,
    nama        VARCHAR(255) NOT NULL,
    induk_id    BIGINT REFERENCES contoh(id),

    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  BIGINT NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by  BIGINT NOT NULL,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

-- Pencarian publik: equality-only, tanpa unique.
CREATE INDEX idx_contoh_uuid ON contoh USING hash (uuid);

-- FK tidak otomatis di-index PostgreSQL.
CREATE INDEX idx_contoh_induk_id ON contoh (induk_id);

-- Unik hanya untuk baris aktif, jadi nilai boleh dipakai lagi setelah soft delete.
CREATE UNIQUE INDEX ux_contoh_nama_active ON contoh (nama) WHERE deleted_at IS NULL;
```

### Aturan turunan

1. **Jangan pernah membocorkan `id`.** DTO response memakai `uuid`, request memakai `*_uuid`, dan
   path parameter memakai `uuid` (mis. `GET /contoh/data/{uuid}`). Field `id` dan semua kolom
   `*_id` hanya untuk internal (entity, service, repository, SQL). Jangan mengandalkan
   `@JsonIgnore`; bentuk DTO-nya yang harus tidak punya field tersebut.
2. **FK tetap `*_id` di database.** Relasi JPA memakai `@ManyToOne` + `@JoinColumn(name = "..._id")`.
   Service me-resolve `*_uuid` dari request menjadi entity sebelum menyimpan. Jangan menerima
   `*_id` dari client. Kolom `*_uuid` denormalisasi hanya boleh ditambahkan kalau ada alasan
   performa, dan wajib dijaga konsisten.
3. **Soft delete, bukan hard delete.** Hapus = isi `deleted_at` + `deleted_by`. `BaseEntity` sudah
   memasang `@SQLRestriction("deleted_at IS NULL")`, jadi query Hibernate otomatis menyaring data
   aktif; native query wajib menambahkan predicate sendiri. Jangan memakai `DELETE FROM` untuk
   data domain.
4. **Unique = partial index.** Index unik selalu `WHERE deleted_at IS NULL` supaya nilai unik bisa
   dipakai ulang setelah data lama dihapus. Untuk unik case-insensitive, index ekspresi
   `lower(kolom)` dengan predicate yang sama.
5. **Audit diisi server.** `created_*`/`updated_*`/`deleted_*` diisi di satu tempat (audit listener
   atau `@PrePersist`/`@PreUpdate`), memakai `OffsetDateTime` (bukan `LocalDateTime`). Kolom
   `*_by` berasal dari principal yang sedang login, bukan body request. Selama auth belum ada,
   pakai `0L` untuk aksi sistem/seed dan siapkan abstraksi `CurrentUser` saat auth ditambahkan.
   Seluruh kolom audit (`*_at` dan `*_by`) tidak pernah diekspos ke client.
6. **MapStruct mengabaikan kolom internal.** Saat memetakan request ke entity, `id`, `uuid`, dan
   semua kolom audit harus `ignore`; `uuid`/audit diisi service/listener. Entity turunan sebaiknya
   cukup `@Getter`/`@Setter` (bukan `@Data`) supaya `equals`/`hashCode` tidak memakai field mutable.

`Salam` dan `V1__create_salam_table.sql` adalah contoh lama (`GENERATED BY DEFAULT`, `dibuat_pada`)
dan bukan acuan entity baru. Kalau `Salam` disentuh, migrasikan bertahap lewat migrasi Flyway baru;
jangan menyalin polanya ke tabel baru.

## Aturan

1. **Skema dikelola Flyway, bukan Hibernate.** Hibernate memakai `schema-management.strategy=validate`.
   Perubahan skema harus berupa migrasi baru di `src/main/resources/db/migration`
   (`V2__...sql`), jangan mengubah migrasi yang sudah ada dan jangan mengandalkan auto-DDL.
2. **Entity tidak boleh keluar dari REST layer.** Resource hanya mengembalikan DTO; field internal
   seperti `catatanInternal`, `id`, semua `*_id`, dan seluruh kolom audit (`created_*`, `updated_*`,
   `deleted_*`) sengaja tidak ada di DTO. Identitas publik yang dipakai client adalah
   `uuid`/`*_uuid` (lihat bagian model data di atas).
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
10. **`id` internal, `uuid` publik.** Jangan mengekspos `id` atau `*_id` lewat DTO, query param,
    path, header, maupun pesan error; gunakan `uuid`/`*_uuid`.
11. **Data domain tidak dihapus fisik.** Soft delete lewat `deleted_at`/`deleted_by`; `BaseEntity`
    memasang `@SQLRestriction("deleted_at IS NULL")` supaya query Hibernate otomatis hanya membaca
    data aktif. Index unik baru harus partial (`WHERE deleted_at IS NULL`).
12. **Kolom audit diisi server**, termasuk `created_by`/`updated_by`/`deleted_by` dari principal —
    jangan pernah menerimanya dari body request.
