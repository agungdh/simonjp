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
class PangkatGolonganResourceTest extends ApiTestSupport {

    @Test
    void crudPangkatGolongan() {
        String golongan = shortToken(9);
        String ruang = shortToken(9);

        String uuid = createPangkatGolongan(golongan, ruang);

        given().when().get("/pangkat-golongan/{uuid}", uuid)
                .then().statusCode(200)
                .body("uuid", is(uuid))
                .body("golongan", is(golongan))
                .body("ruang", is(ruang));

        String golonganBaru = shortToken(9);
        String ruangBaru = shortToken(9);
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "jenjang", "Jenjang Baru",
                        "pangkat", "Pangkat Baru",
                        "golongan", golonganBaru,
                        "ruang", ruangBaru))
                .when().put("/pangkat-golongan/{uuid}", uuid)
                .then().statusCode(200)
                .body("golongan", is(golonganBaru))
                .body("ruang", is(ruangBaru));

        given().when().delete("/pangkat-golongan/{uuid}", uuid).then().statusCode(204);
        given().when().get("/pangkat-golongan/{uuid}", uuid)
                .then().statusCode(404)
                .body("message", is("Pangkat golongan tidak ditemukan."));
    }

    @Test
    void kodeDuplikatDitolak() {
        String golongan = shortToken(9);
        String ruang = shortToken(9);
        createPangkatGolongan(golongan, ruang);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "jenjang", "Jenjang Lain",
                        "pangkat", "Pangkat Lain",
                        "golongan", golongan,
                        "ruang", ruang))
                .when().post("/pangkat-golongan")
                .then().statusCode(409)
                .body("message", is("Pangkat golongan sudah ada."));
    }

    @Test
    void cursorPagination() {
        String search = shortToken(9);
        createPangkatGolongan(search, "a");
        createPangkatGolongan(search, "b");
        createPangkatGolongan(search, "c");

        String nextCursor = given()
                .queryParam("search", search)
                .queryParam("limit", 2)
                .when().get("/pangkat-golongan")
                .then()
                .statusCode(200)
                .body("items.size()", is(2))
                .body("hasMore", is(true))
                .body("nextCursor", notNullValue())
                .extract().path("nextCursor");

        given()
                .queryParam("search", search)
                .queryParam("limit", 2)
                .queryParam("cursor", nextCursor)
                .when().get("/pangkat-golongan")
                .then()
                .statusCode(200)
                .body("items.size()", is(1))
                .body("hasMore", is(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void deletePangkatGolonganYangDipakaiPegawaiDitolak() {
        String bidangUuid = createBidang(unique("BIDANG PANGKAT"));
        String pangkatUuid = createPangkatGolongan(shortToken(9), shortToken(9));
        String pegawaiUuid = createPegawai(bidangUuid, pangkatUuid, unique("Pegawai Pangkat"));

        given().when().delete("/pangkat-golongan/{uuid}", pangkatUuid)
                .then().statusCode(409)
                .body("message", is("Pangkat golongan masih dipakai pegawai aktif."));

        given().when().delete("/pegawai/{uuid}", pegawaiUuid).then().statusCode(204);
    }

    @Test
    void validasiRequestKosong() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("jenjang", "", "pangkat", "", "golongan", "", "ruang", ""))
                .when().post("/pangkat-golongan")
                .then().statusCode(400);
    }
}
