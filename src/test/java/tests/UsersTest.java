package tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Users API Tests")
public class UsersTest extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";

    // -------------------------------------------------------
    // GET /users — all users
    // -------------------------------------------------------
    @Test
    @Order(1)
    @DisplayName("GET /users - should return 200 and a non-empty list")
    public void getAllUsers_shouldReturn200AndNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get(USERS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].id", notNullValue())
            .body("[0].email", notNullValue())
            .body("[0].username", notNullValue());
    }

    // -------------------------------------------------------
    // GET /users/{id} — single user
    // -------------------------------------------------------
    @Test
    @Order(2)
    @DisplayName("GET /users/1 - should return user with correct structure")
    public void getUserById_shouldReturnCorrectUser() {
        given()
            .spec(requestSpec)
        .when()
            .get(USERS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("email", notNullValue())
            .body("username", notNullValue())
            .body("name.firstname", notNullValue())
            .body("name.lastname", notNullValue())
            .body("address.city", notNullValue());
    }

    // -------------------------------------------------------
    // GET /users?limit=3 — limit
    // -------------------------------------------------------
    @Test
    @Order(3)
    @DisplayName("GET /users?limit=3 - should return exactly 3 users")
    public void getAllUsers_withLimit_shouldReturnLimitedResults() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 3)
        .when()
            .get(USERS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(3));
    }

    // -------------------------------------------------------
    // POST /users — create user
    // -------------------------------------------------------
    @Test
    @Order(4)
    @DisplayName("POST /users - should create user and return new ID")
    public void createUser_shouldReturnNewUserWithId() {
        String newUser = """
                {
                    "email": "testuser@automation.com",
                    "username": "testautomation",
                    "password": "Test@1234",
                    "name": { "firstname": "Test", "lastname": "Automation" },
                    "address": {
                        "city": "Accra",
                        "street": "123 Jenkins Lane",
                        "number": 7,
                        "zipcode": "00233",
                        "geolocation": { "lat": "5.55", "long": "-0.20" }
                    },
                    "phone": "020-000-0000"
                }
                """;

        given()
            .spec(requestSpec)
            .body(newUser)
        .when()
            .post(USERS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("id", notNullValue());
    }

    // -------------------------------------------------------
    // PUT /users/{id} — update user
    // -------------------------------------------------------
    @Test
    @Order(5)
    @DisplayName("PUT /users/1 - should update user email")
    public void updateUser_shouldReturnUpdatedUser() {
        String updatedUser = """
                {
                    "email": "updated@automation.com",
                    "username": "updateduser",
                    "password": "Updated@1234"
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedUser)
        .when()
            .put(USERS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }

    // -------------------------------------------------------
    // DELETE /users/{id} — delete user
    // -------------------------------------------------------
    @Test
    @Order(6)
    @DisplayName("DELETE /users/1 - should delete user and return 200")
    public void deleteUser_shouldReturn200WithDeletedUser() {
        given()
            .spec(requestSpec)
        .when()
            .delete(USERS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }
}
