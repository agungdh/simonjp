package id.my.agungdh;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .contentType(containsString("application/json"))
             .body("message", is("Hello from Quarkus REST"))
             .body("timestamp", notNullValue())
             .body("thread", notNullValue())
             .body("virtualThread", is(true));
    }

}
