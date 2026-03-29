package tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Carts API Tests")
public class CartsTest extends BaseTest {

    private static final String CARTS_ENDPOINT = "/carts";

    // -------------------------------------------------------
    // GET /carts — all carts
    // -------------------------------------------------------
    @Test
    @Order(1)
    @DisplayName("GET /carts - should return 200 and a non-empty list")
    public void getAllCarts_shouldReturn200AndNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get(CARTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].id", notNullValue())
            .body("[0].userId", notNullValue())
            .body("[0].products", notNullValue());
    }

    // -------------------------------------------------------
    // GET /carts/{id} — single cart
    // -------------------------------------------------------
    @Test
    @Order(2)
    @DisplayName("GET /carts/1 - should return cart with correct structure")
    public void getCartById_shouldReturnCorrectCart() {
        given()
            .spec(requestSpec)
        .when()
            .get(CARTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("userId", notNullValue())
            .body("products", notNullValue())
            .body("products", hasSize(greaterThan(0)))
            .body("products[0].productId", notNullValue())
            .body("products[0].quantity", notNullValue());
    }

    // -------------------------------------------------------
    // GET /carts/user/{userId} — carts for a user
    // -------------------------------------------------------
    @Test
    @Order(3)
    @DisplayName("GET /carts/user/1 - should return carts belonging to user 1")
    public void getCartsByUser_shouldReturnUserCarts() {
        given()
            .spec(requestSpec)
        .when()
            .get(CARTS_ENDPOINT + "/user/1")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].userId", equalTo(1));
    }

    // -------------------------------------------------------
    // GET /carts?limit=3 — limit query param
    // -------------------------------------------------------
    @Test
    @Order(4)
    @DisplayName("GET /carts?limit=3 - should return exactly 3 carts")
    public void getAllCarts_withLimit_shouldReturnLimitedResults() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 3)
        .when()
            .get(CARTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(3));
    }

    // -------------------------------------------------------
    // POST /carts — add new cart
    // -------------------------------------------------------
    @Test
    @Order(5)
    @DisplayName("POST /carts - should create a new cart and return ID")
    public void createCart_shouldReturnNewCartWithId() {
        String newCart = """
                {
                    "userId": 5,
                    "date": "2024-11-15",
                    "products": [
                        { "productId": 1, "quantity": 2 },
                        { "productId": 3, "quantity": 1 }
                    ]
                }
                """;

        given()
            .spec(requestSpec)
            .body(newCart)
        .when()
            .post(CARTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    // -------------------------------------------------------
    // PUT /carts/{id} — update cart
    // -------------------------------------------------------
    @Test
    @Order(6)
    @DisplayName("PUT /carts/1 - should update cart products")
    public void updateCart_shouldReturnUpdatedCart() {
        String updatedCart = """
                {
                    "userId": 3,
                    "date": "2024-11-20",
                    "products": [
                        { "productId": 5, "quantity": 3 }
                    ]
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedCart)
        .when()
            .put(CARTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }

    // -------------------------------------------------------
    // DELETE /carts/{id} — delete cart
    // -------------------------------------------------------
    @Test
    @Order(7)
    @DisplayName("DELETE /carts/1 - should delete cart and return 200")
    public void deleteCart_shouldReturn200WithDeletedCart() {
        given()
            .spec(requestSpec)
        .when()
            .delete(CARTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }
}
