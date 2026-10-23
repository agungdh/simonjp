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
class BidangResourceTest extends ApiTestSupport {

    @Test
    void crudBidang() {
        String nama = unique("BIDANG UJI");

        String uuid = given()
                .contentType(ContentType.JSON)
                .body(Map.of("bidang", nama))
                .when().post("/bidang")
                .then()
                .statusCode(201)
                .body("uuid", notNullValue())
                .body("bidang", is(nama))
                .extract().path("uuid");

        given().when().get("/bidang/{uuid}", uuid)
                .then().statusCode(200)
                .body("uuid", is(uuid))
                .body("bidang", is(nama));

        String namaBaru = unique("BIDANG UBAH");
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("bidang", namaBaru))
                .when().put("/bidang/{uuid}", uuid)
                .then().statusCode(200)
                .body("bidang", is(namaBaru));

        given().when().delete("/bidang/{uuid}", uuid).then().statusCode(204);
        given().when().get("/bidang/{uuid}", uuid)
                .then().statusCode(404)
                .body("message", is("Bidang tidak ditemukan."));
    }

    @Test
    void namaBidangDuplikatDitolak() {
        String nama = unique("BIDANG DOBEL");
        createBidang(nama);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("bidang", nama.toLowerCase()))
                .when().post("/bidang")
                .then().statusCode(409)
                .body("message", is("Bidang sudah ada."));
    }

    @Test
    void validasiRequestKosong() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("bidang", "  "))
                .when().post("/bidang")
                .then().statusCode(400);
    }

    @Test
    void cursorPagination() {
        String search = unique("CURSOR-BIDANG");
        createBidang(search + " A");
        createBidang(search + " B");
        createBidang(search + " C");

        String nextCursor = given()
                .queryParam("search", search)
                .queryParam("limit", 2)
                .when().get("/bidang")
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
                .when().get("/bidang")
                .then()
                .statusCode(200)
                .body("items.size()", is(1))
                .body("hasMore", is(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void cursorTidakValidDitolak() {
        given()
                .queryParam("cursor", "00000000-0000-0000-0000-000000000000")
                .when().get("/bidang")
                .then().statusCode(400)
                .body("message", is("Cursor tidak valid."));
    }

    @Test
    void deleteBidangYangDipakaiPegawaiDitolak() {
        String bidangUuid = createBidang(unique("BIDANG TERPAKAI"));
        String pegawaiUuid = createPegawai(bidangUuid, null, unique("Pegawai Terpakai"));

        given().when().delete("/bidang/{uuid}", bidangUuid)
                .then().statusCode(409)
                .body("message", is("Bidang masih dipakai pegawai aktif."));

        given().when().delete("/pegawai/{uuid}", pegawaiUuid).then().statusCode(204);
    }
}
