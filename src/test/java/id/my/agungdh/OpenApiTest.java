package id.my.agungdh;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OpenApiTest {

    @Test
    void dokumenOpenApiTersedia() {
        given()
                .when().get("/q/openapi?format=json")
                .then()
                .statusCode(200)
                .body("info.title", is("SimonJP API"))
                .body("info.version", is("1.0.0"));
    }

    @Test
    void swaggerUiTersedia() {
        given()
                .when().get("/q/swagger-ui/")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("id=\"swagger-ui\""));
    }
}
