package id.my.agungdh.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataResourceTest {

    @Test
    void listMengembalikanDtoTanpaFieldInternal() {
        given()
                .when().get("/contoh/data")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("nama", hasItems("Agung", "Budi", "Citra"))
                .body("[0].catatanInternal", nullValue())
                .body("[0].dibuatPada", notNullValue());
    }

    @Test
    void postMemetakanRequestKeEntityLaluKembaliKeDto() {
        Number id = given()
                .contentType("application/json")
                .body("""
                        {"nama":"Dewi","pesan":"Halo dari POST"}
                        """)
                .when().post("/contoh/data")
                .then()
                .statusCode(201)
                .body("nama", is("Dewi"))
                .body("pesan", is("Halo dari POST"))
                .body("id", notNullValue())
                .body("dibuatPada", notNullValue())
                .body("catatanInternal", nullValue())
                .extract().path("id");

        assertTrue(id.longValue() >= 4, "id harus di-generate oleh server");
    }

    @Test
    void idYangTidakAdaMengembalikan404() {
        given()
                .when().get("/contoh/data/999")
                .then()
                .statusCode(404);
    }
}
