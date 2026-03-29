package tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth API Tests")
public class AuthTest extends BaseTest {

    private static final String AUTH_ENDPOINT = "/auth/login";

    // -------------------------------------------------------
    // POST /auth/login — valid credentials
    // -------------------------------------------------------
    @Test
    @Order(1)
    @DisplayName("POST /auth/login - valid credentials should return a token")
    public void login_withValidCredentials_shouldReturnToken() {
        String validCredentials = """
                {
                    "username": "mor_2314",
                    "password": "83r5^_"
                }
                """;

        Response response = given()
            .spec(requestSpec)
            .body(validCredentials)
        .when()
            .post(AUTH_ENDPOINT)
        .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("token", not(emptyString()))
            .extract().response();

        String token = response.jsonPath().getString("token");
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");

        System.out.println("Auth token received: " + token.substring(0, 20) + "...");
    }

    // -------------------------------------------------------
    // POST /auth/login — invalid credentials
    // -------------------------------------------------------
    @Test
    @Order(2)
    @DisplayName("POST /auth/login - invalid credentials should return error")
    public void login_withInvalidCredentials_shouldReturnError() {
        String invalidCredentials = """
                {
                    "username": "wronguser",
                    "password": "wrongpassword123"
                }
                """;

        Response response = given()
            .spec(requestSpec)
            .body(invalidCredentials)
        .when()
            .post(AUTH_ENDPOINT)
        .then()
            .extract().response();

        // FakeStore returns 401 for invalid credentials
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 400 || statusCode == 401,
                "Expected 400 or 401 for invalid credentials, got: " + statusCode);
    }

    // -------------------------------------------------------
    // POST /auth/login — missing fields
    // -------------------------------------------------------
    @Test
    @Order(3)
    @DisplayName("POST /auth/login - missing password field should return error")
    public void login_withMissingFields_shouldReturnError() {
        String missingPassword = """
                {
                    "username": "mor_2314"
                }
                """;

        Response response = given()
            .spec(requestSpec)
            .body(missingPassword)
        .when()
            .post(AUTH_ENDPOINT)
        .then()
            .extract().response();

        // Should not succeed with missing fields
        assertNotEquals(200, response.getStatusCode(),
                "Login should fail when password is missing");
    }

    // -------------------------------------------------------
    // POST /auth/login — empty body
    // -------------------------------------------------------
    @Test
    @Order(4)
    @DisplayName("POST /auth/login - empty body should return error")
    public void login_withEmptyBody_shouldReturnError() {
        given()
            .spec(requestSpec)
            .body("{}")
        .when()
            .post(AUTH_ENDPOINT)
        .then()
            .statusCode(not(200));
    }
}
