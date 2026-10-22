package id.my.agungdh;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OpenApiTest {

    @Test
    void dokumenOpenApiMemuatSemuaEndpoint() {
        given()
                .when().get("/q/openapi?format=json")
                .then()
                .statusCode(200)
                .body("info.title", is("SimonJP API"))
                .body("paths", hasKey("/hello"))
                .body("paths", hasKey("/contoh/data"))
                .body("paths", hasKey("/contoh/data/{id}"));
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
