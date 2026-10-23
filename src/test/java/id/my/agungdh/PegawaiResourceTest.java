package id.my.agungdh;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class PegawaiResourceTest extends ApiTestSupport {

    @Test
    void crudPegawai() {
        String bidangUuid = createBidang(unique("BIDANG PEGAWAI"));
        String pangkatUuid = createPangkatGolongan(shortToken(9), shortToken(9));
        String nip = nip();
        String nama = unique("Pegawai Uji");

        String uuid = given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, nama, bidangUuid, pangkatUuid))
                .when().post("/pegawai")
                .then()
                .statusCode(201)
                .body("uuid", notNullValue())
                .body("nip", is(nip))
                .body("nama", is(nama))
                .body("tipe", is("PEGAWAI"))
                .body("status", is("AKTIF"))
                .body("bidang.uuid", is(bidangUuid))
                .body("pangkatGolongan.uuid", is(pangkatUuid))
                .body("kategoriJabatan", is("FUNGSIONAL_AUDITOR"))
                .extract().path("uuid");

        given().when().get("/pegawai/{uuid}", uuid)
                .then().statusCode(200)
                .body("uuid", is(uuid))
                .body("nip", is(nip));

        String namaBaru = unique("Pegawai Ubah");
        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, namaBaru, bidangUuid, null))
                .when().put("/pegawai/{uuid}", uuid)
                .then().statusCode(200)
                .body("nama", is(namaBaru))
                .body("pangkatGolongan", nullValue());

        given().when().delete("/pegawai/{uuid}", uuid).then().statusCode(204);
        given().when().get("/pegawai/{uuid}", uuid)
                .then().statusCode(404)
                .body("message", is("Pegawai tidak ditemukan."));
    }

    @Test
    void nipDuplikatDitolak() {
        String bidangUuid = createBidang(unique("BIDANG NIP"));
        String nip = nip();

        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Dobel"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Dobel 2"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(409)
                .body("message", is("NIP sudah digunakan."));
    }

    @Test
    void bidangTidakAdaDitolak() {
        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip(), unique("Pegawai Tanpa Bidang"),
                        "00000000-0000-0000-0000-000000000000", null))
                .when().post("/pegawai")
                .then().statusCode(400)
                .body("message", is("Bidang tidak ditemukan."));
    }

    @Test
    void validasiNipBukanAngka() {
        String bidangUuid = createBidang(unique("BIDANG VALIDASI"));

        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload("12AB", unique("Pegawai Salah NIP"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(400);
    }

    @Test
    void filterDanCursorPagination() {
        String bidangUuid = createBidang(unique("BIDANG FILTER"));
        String token = shortToken(8);
        createPegawai(bidangUuid, null, "Pegawai " + token + " A");
        createPegawai(bidangUuid, null, "Pegawai " + token + " B");
        createPegawai(bidangUuid, null, "Pegawai " + token + " C");

        String nextCursor = given()
                .queryParam("search", token)
                .queryParam("status", "AKTIF")
                .queryParam("bidangUuid", bidangUuid)
                .queryParam("limit", 2)
                .when().get("/pegawai")
                .then()
                .statusCode(200)
                .body("items.size()", is(2))
                .body("hasMore", is(true))
                .body("nextCursor", notNullValue())
                .extract().path("nextCursor");

        given()
                .queryParam("search", token)
                .queryParam("status", "AKTIF")
                .queryParam("bidangUuid", bidangUuid)
                .queryParam("limit", 2)
                .queryParam("cursor", nextCursor)
                .when().get("/pegawai")
                .then()
                .statusCode(200)
                .body("items.size()", is(1))
                .body("hasMore", is(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void cursorDariFilterLainDitolak() {
        String bidangA = createBidang(unique("BIDANG CURSOR A"));
        String bidangB = createBidang(unique("BIDANG CURSOR B"));
        String pegawaiUuid = createPegawai(bidangA, null, unique("Pegawai Cursor"));

        given()
                .queryParam("bidangUuid", bidangB)
                .queryParam("cursor", pegawaiUuid)
                .when().get("/pegawai")
                .then().statusCode(400)
                .body("message", is("Cursor tidak valid."));
    }

    @Test
    void softDeleteTidakMenghapusPermanen() {
        String bidangUuid = createBidang(unique("BIDANG SOFT DELETE"));
        String nip = nip();
        String pegawaiUuid = given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Soft Delete"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(201)
                .extract().path("uuid");

        given().when().delete("/pegawai/{uuid}", pegawaiUuid).then().statusCode(204);

        // NIP yang sama boleh dipakai lagi setelah pegawai lama di-soft delete.
        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Baru"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(201);
    }

    @Test
    void updatePegawaiTidakBolehMemakaiNipSendiri() {
        String bidangUuid = createBidang(unique("BIDANG UPDATE NIP"));
        String nip = nip();
        String pegawaiUuid = given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Update NIP"), bidangUuid, null))
                .when().post("/pegawai")
                .then().statusCode(201)
                .extract().path("uuid");

        given()
                .contentType(ContentType.JSON)
                .body(pegawaiPayload(nip, unique("Pegawai Update NIP 2"), bidangUuid, null))
                .when().put("/pegawai/{uuid}", pegawaiUuid)
                .then().statusCode(200)
                .body("nip", is(nip));
    }

    @Test
    void lookupListMasterDapatDipakai() {
        given().queryParam("limit", 100)
                .when().get("/bidang")
                .then().statusCode(200)
                .body("items", notNullValue());

        given().queryParam("limit", 100)
                .when().get("/pangkat-golongan")
                .then().statusCode(200)
                .body("items", notNullValue());
    }

    @Test
    void bodyTanpaFieldWajibDitolak() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nama", "Tanpa NIP"))
                .when().post("/pegawai")
                .then().statusCode(400);
    }
}
