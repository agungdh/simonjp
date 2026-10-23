# AGENTS.md

Instruksi kerja untuk coding agent. `README.md` adalah dokumentasi untuk manusia; file ini fokus
pada hal yang dibutuhkan saat mengubah kode.

## Stack

- Quarkus 3.40.1.1 (BOM di `gradle.properties`), Java 25, Gradle Kotlin DSL (`build.gradle.kts`).
- REST: `quarkus-rest` + `quarkus-rest-jackson`; OpenAPI: `quarkus-smallrye-openapi` + `quarkus-swagger-ui`.
- Persistensi: Hibernate ORM (JPA), PostgreSQL, Flyway.
- Lombok (`compileOnly`) + MapStruct, semuanya CDI bean.
- Virtual threads: semua REST resource blocking memakai `@RunOnVirtualThread` di level class.

## Perintah

```shell
docker compose up -d postgres     # wajib sebelum test / dev
./gradlew quarkusDev              # dev mode, http://localhost:8080
./gradlew test                    # butuh Postgres hidup
./gradlew build                   # build + test
./gradlew build -Dquarkus.native.enabled=true   # native (GraalVM)
```

`src/native-test` berisi integration test terpisah (`*IT`), dijalankan hanya pada build native.

## Struktur & arsitektur

Layered architecture sederhana, dependensi satu arah:

```text
resource/ → service/ → repository/ → domain/
```

Semua kode produksi ada di `src/main/java/id/my/agungdh/`:

| Paket | Isi | Boleh bergantung pada |
| --- | --- | --- |
| `resource/` | JAX-RS resource (`GreetingResource`, `DataResource`), hanya HTTP + DTO | `service/`, `dto/` |
| `service/` | Business logic, transaksi, resolve `*Uuid` ke entity | `repository/`, `domain/`, `dto/`, `mapper/` |
| `repository/` | Akses DB (Hibernate/`EntityManager`), query | `domain/` |
| `domain/` | Entity JPA. Entity baru mewarisi `BaseEntity` (identitas + audit); `Salam` masih contoh lama. Mutable, memakai Lombok. | tidak ada layer aplikasi lain |
| `dto/` | `record` immutable request/response (`SalamDTO`, `SalamRequest`, `HelloDTO`), camelCase | tidak ada layer aplikasi lain |
| `mapper/` | Interface MapStruct (`componentModel = "cdi"`) | `domain/`, `dto/` |

Aturan layer:

1. Resource hanya menangani HTTP, validasi input, dan DTO; jangan akses `EntityManager`/repository
   langsung.
2. Service memegang business logic dan batas transaksi (`@Transactional`); boleh bekerja dengan
   entity, tetapi nilai yang keluar ke resource harus DTO.
3. Repository hanya SQL/JPA dan mengembalikan entity, bukan DTO; jangan import `resource/` atau
   `dto/`.
4. Domain tidak boleh import `service/`, `repository/`, `resource/`, atau `dto/`.
5. Jangan menambah port/adapter/use-case interface ala hexagonal/clean architecture; layered
   sederhana ini sudah cukup kecuali ada kebutuhan nyata.

## Virtual threads, Java 25 & mapping

- **Semua REST resource memakai virtual threads.** Pasang `@RunOnVirtualThread` di level class
  (seperti `GreetingResource`/`DataResource`), bukan per method, supaya konsisten. Endpoint harus
  blocking dan mengembalikan DTO/`Response`, bukan `Uni`/`Multi`.
- **Virtual thread cocok untuk CRUD** karena beban utamanya blocking I/O (JDBC, Hibernate, HTTP
  client). Ini bukan percepatan CPU: jangan menjalankan generate PDF/Excel, kompresi, atau
  perhitungan berat di service ini. Delegasikan ke service lain lewat HTTP client; panggilan I/O-nya
  tetap aman di virtual thread.
- Virtual thread tidak menambah kapasitas database; tetap atur ukuran connection pool.
- `@Transactional` tetap di layer service, bukan resource.
- **DTO selalu `record` immutable**, termasuk request, response, dan wrapper pagination. Jangan
  memakai Lombok/`@Data` di DTO. Field camelCase dan beri `@Schema` untuk OpenAPI.
- **Konversi entity ↔ DTO hanya lewat MapStruct** di `mapper/` (`componentModel = "cdi"`). Resource
  tidak boleh mapping manual; service memakai mapper. Saat request → entity, `id`, `uuid`, dan kolom
  audit wajib `ignore`; `uuid`/audit diisi service/listener.
- **Java 25 adalah target.** Jangan menurunkan `sourceCompatibility`/`targetCompatibility`; gunakan
  `record`, pattern matching, dan fitur Java 25 lain yang stabil.

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

### Naming

- **Tabel: singular**, snake_case: `salam`, `order_item`, `user_role`. Jangan memakai bentuk plural
  (`users`, `order_items`); ini mengikuti nama entity dan tabel `salam` yang sudah ada.
- Hindari nama tabel yang bentrok dengan reserved word SQL; pakai nama domain yang lebih spesifik
  (`purchase_order`, bukan `order`).
- **Kolom: snake_case**: `induk_id`, `nomor_telepon`, `created_at`.
- **Entity: PascalCase singular** (`OrderItem`) dengan `@Table(name = "order_item")` eksplisit.
- **DTO/JSON: camelCase** (`indukUuid`, `nomorTelepon`).
- **Index/constraint**: `pk_<tabel>`, `fk_<tabel>_<kolom>`, `ux_<tabel>_<kolom>`, `idx_<tabel>_<kolom>`;
  index unik yang hanya berlaku untuk data aktif memakai suffix `_active`.

### Aturan turunan

1. **Jangan pernah membocorkan `id`.** DTO response memakai `uuid`, request memakai `*Uuid` dalam
   camelCase (mis. `indukUuid`), dan path parameter memakai `uuid` (mis.
   `GET /contoh/data/{uuid}`). Field `id` dan semua kolom `*_id` hanya untuk internal (entity,
   service, repository, SQL). Jangan mengandalkan `@JsonIgnore`; bentuk DTO-nya yang harus tidak
   punya field tersebut.
2. **FK tetap `*_id` di database.** Relasi JPA memakai `@ManyToOne` + `@JoinColumn(name = "..._id")`.
   Service me-resolve `*Uuid` dari request menjadi entity sebelum menyimpan. Jangan menerima
   `*_id` dari client. Kolom DB `*_uuid` denormalisasi hanya boleh ditambahkan kalau ada alasan
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

## Konvensi DTO & JSON

- Nama field DTO (record component) dan JSON memakai camelCase: `indukUuid`, `nomorTelepon`.
- Nama tabel dan kolom database tetap snake_case: `order_item`, `induk_id`, `nomor_telepon`; jangan
  mengubahnya menjadi camelCase. Tabel memakai bentuk singular (lihat bagian naming).
- Jangan menambahkan `@JsonProperty`/`@JsonNaming` untuk memaksa snake_case. Biarkan Jackson
  memakai nama record apa adanya.
- Query parameter multi-kata memakai camelCase (`pageSize`, `sortBy`); path parameter identitas
  tetap `uuid`.
- MapStruct memetakan otomatis kalau nama field DTO dan entity sama; kalau beda, pakai
  `@Mapping(source = "...", target = "...")`.

## Konvensi REST

REST tidak mewajibkan bentuk plural; itu hanya konvensi gaya. Yang penting konsisten dan mudah
diprediksi.

- Path resource memakai **singular** dan kebab-case: `/order-item`, `/order-item/{uuid}`. Jangan
  mencampur singular dan plural.
- Aksi ditentukan HTTP method, bukan verb di path: `GET /order-item`, `POST /order-item`,
  `GET /order-item/{uuid}`, `PATCH /order-item/{uuid}`, `DELETE /order-item/{uuid}`.
- Path parameter untuk identitas selalu `uuid`, bukan `id`.
- Path lama (`/contoh/data`, `/hello`) tidak perlu di-rename hanya demi konvensi baru; terapkan
  aturan ini ke endpoint baru.

## Pagination (cursor)

Endpoint list yang datanya bisa besar memakai **cursor pagination**, bukan offset/page. Offset
semakin lambat seiring halaman dan tidak cocok untuk infinite scroll.

- Cursor yang diekspos ke client hanya `uuid` baris terakhir. Jangan pernah mengirim `id`, offset,
  atau nomor halaman di cursor.
- Query param: `cursor` (UUID, opsional), `limit` (default 20, maksimum 100), `sort`, dan `order`
  (`asc`/`desc`).
- Kalau `sort` tidak dikirim, default `ORDER BY id DESC`; `order` default `desc`. `id` tetap
  internal dan tidak diekspos ke client.
- Saat `cursor` ada, service:
  1. load baris cursor berdasarkan `uuid` (lookup hash index);
  2. kalau tidak ada atau sudah soft-delete, kembalikan `400` cursor tidak valid — bukan `404`;
  3. pastikan baris cursor masih cocok dengan filter yang sama; kalau tidak, `400`;
  4. pakai `id` (dan nilai kolom sort) baris itu sebagai posisi keyset;
  5. query halaman berikutnya dengan filter + predicate keyset + `ORDER BY` yang sama.
- Cursor hanya valid untuk kombinasi filter + sort yang sama. Kalau client mengubah filter/sort,
  reset cursor (kirim request tanpa `cursor`).
- Selalu tambahkan `id` sebagai tiebreaker unik di akhir `ORDER BY`, dan gunakan tuple comparison
  untuk sort non-unik.
- Ambil `limit + 1` baris untuk menentukan `hasMore`, lalu buang baris ekstra. `nextCursor` adalah
  `uuid` baris terakhir yang benar-benar dikembalikan, atau `null` kalau tidak ada lagi.
- Filter wajib diterapkan juga saat lookup cursor; jangan memakai posisi baris yang tidak terlihat
  oleh filter saat ini.
- Karena `uuid` tidak unique di database, lookup cursor jangan memakai `getSingleResult()`; ambil
  satu baris secara deterministik (`ORDER BY id LIMIT 1`) atau tangani duplikat.
- `sort` hanya menerima field yang di-whitelist; jangan interpolasi nama kolom dari request langsung
  ke JPQL/SQL. Jangan menjadikan `uuid` sebagai sort utama karena UUID v4 acak, bukan urutan waktu.
- Jangan memakai offset untuk list utama. Offset hanya boleh untuk tabel kecil/admin atau kalau
  memang diminta eksplisit.

Bentuk response:

```json
{
  "items": [],
  "nextCursor": "0f1e2d3c-...",
  "hasMore": true
}
```

DTO-nya bisa generik:

```java
public record CursorPageDTO<T>(List<T> items, UUID nextCursor, boolean hasMore) {
}
```

Contoh keyset default (`sort` tidak dikirim → `ORDER BY id DESC`):

```sql
SELECT ...
FROM order_item
WHERE /* filter */
  AND (:cursorId IS NULL OR id < :cursorId)
ORDER BY id DESC
LIMIT :limitPlusOne;
-- deleted_at IS NULL sudah ditambahkan otomatis oleh @SQLRestriction
```

Untuk sort non-unik:

```sql
WHERE (sort_col, id) < (:cursorSort, :cursorId)
ORDER BY sort_col DESC, id DESC
```

Index pendukung:

- lookup cursor: hash index `uuid` yang sudah ada;
- keyset default: PK `id`; kalau banyak baris soft-delete, partial index `(id) WHERE deleted_at IS NULL`;
- sort lain: btree `(sort_col, id) WHERE deleted_at IS NULL`.

Endpoint lama `/contoh/data` masih mengembalikan list biasa; endpoint list baru memakai wrapper
`CursorPageDTO`.

## Aturan

1. **Skema dikelola Flyway, bukan Hibernate.** Hibernate memakai `schema-management.strategy=validate`.
   Perubahan skema harus berupa migrasi baru di `src/main/resources/db/migration`
   (`V2__...sql`), jangan mengubah migrasi yang sudah ada dan jangan mengandalkan auto-DDL.
2. **Entity tidak boleh keluar dari REST layer.** Resource hanya mengembalikan DTO; field internal
   seperti `catatanInternal`, `id`, semua `*_id`, dan seluruh kolom audit (`created_*`, `updated_*`,
   `deleted_*`) sengaja tidak ada di DTO. Identitas publik yang dipakai client adalah
   `uuid`/`*Uuid` (lihat bagian model data di atas).
3. **`SalamService` masih in-memory** walaupun `Salam` sudah berupa entity JPA. Jangan berasumsi data
   sudah tersimpan ke database; kalau butuh persistensi, tambahkan repository/`EntityManager` secara
   eksplisit dan sesuaikan test.
4. **Urutan annotation processor penting** di `build.gradle.kts`: Lombok → `lombok-mapstruct-binding`
   → MapStruct. Mengubah urutan ini membuat mapper gagal melihat getter/builder hasil Lombok.
5. **Test memakai PostgreSQL asli**, bukan H2/in-memory. Test Quarkus (`@QuarkusTest`) akan gagal
   kalau `docker compose up -d postgres` belum dijalankan.
6. **Java 25 + virtual threads:** jangan turunkan compatibility; pakai `record` untuk DTO,
   `@RunOnVirtualThread` di level class untuk semua resource, dan MapStruct untuk entity ↔ DTO
   (detail di bagian virtual threads di atas).
7. Encoding UTF-8 dan compiler flag `-parameters` sudah diatur di `build.gradle.kts`; jangan
   menghapusnya (dibutuhkan Jackson/MapStruct).
8. Komentar dan nama endpoint di repo ini campur Indonesia–Inggris. Ikuti gaya di file sekitarnya,
   jangan menyeragamkan yang sudah ada.
9. Jangan commit `build/`, `.gradle/`, atau artefak hasil generate.
10. **`id` internal, `uuid` publik.** Jangan mengekspos `id` atau `*_id` lewat DTO, query param,
    path, header, maupun pesan error; gunakan `uuid`/`*Uuid` (`*_uuid` hanya nama kolom database).
11. **Data domain tidak dihapus fisik.** Soft delete lewat `deleted_at`/`deleted_by`; `BaseEntity`
    memasang `@SQLRestriction("deleted_at IS NULL")` supaya query Hibernate otomatis hanya membaca
    data aktif. Index unik baru harus partial (`WHERE deleted_at IS NULL`).
12. **Kolom audit diisi server**, termasuk `created_by`/`updated_by`/`deleted_by` dari principal —
    jangan pernah menerimanya dari body request.
13. **List endpoint memakai cursor pagination.** Cursor adalah `uuid` baris terakhir; jangan
    memakai offset/page untuk list utama. Detail di bagian pagination di atas.
14. **Kerja CPU-heavy (generate PDF/Excel, kompresi) tidak dijalankan di service ini.** Delegasikan
    ke service lain lewat HTTP client; virtual thread hanya untuk blocking I/O CRUD.
