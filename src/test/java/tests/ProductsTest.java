package tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Products API Tests")
public class ProductsTest extends BaseTest {

    private static final String PRODUCTS_ENDPOINT = "/products";

    // -------------------------------------------------------
    // GET /products — fetch all products
    // -------------------------------------------------------
    @Test
    @Order(1)
    @DisplayName("GET /products - should return 200 and a non-empty list")
    public void getAllProducts_shouldReturn200AndNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get(PRODUCTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].id", notNullValue())
            .body("[0].title", notNullValue())
            .body("[0].price", greaterThan(0.0f));
    }

    // -------------------------------------------------------
    // GET /products/{id} — fetch single product
    // -------------------------------------------------------
    @Test
    @Order(2)
    @DisplayName("GET /products/1 - should return correct product fields")
    public void getProductById_shouldReturnCorrectProduct() {
        given()
            .spec(requestSpec)
        .when()
            .get(PRODUCTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", notNullValue())
            .body("price", greaterThan(0.0f))
            .body("category", notNullValue())
            .body("image", notNullValue())
            .body("rating.rate", notNullValue());
    }

    // -------------------------------------------------------
    // GET /products/{id} — invalid product ID
    // -------------------------------------------------------
    @Test
    @Order(3)
    @DisplayName("GET /products/9999 - invalid ID should return null or 404")
    public void getProductById_withInvalidId_shouldHandleGracefully() {
        Response response = given()
            .spec(requestSpec)
        .when()
            .get(PRODUCTS_ENDPOINT + "/9999")
        .then()
            .extract().response();

        // FakeStore returns "null" body with 200 for unknown IDs
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 404,
                "Expected 200 or 404 but got: " + statusCode);
    }

    // -------------------------------------------------------
    // GET /products?limit=5 — limit query param
    // -------------------------------------------------------
    @Test
    @Order(4)
    @DisplayName("GET /products?limit=5 - should return exactly 5 products")
    public void getAllProducts_withLimit_shouldReturnLimitedResults() {
        given()
            .spec(requestSpec)
            .queryParam("limit", 5)
        .when()
            .get(PRODUCTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("$", hasSize(5));
    }

    // -------------------------------------------------------
    // GET /products/categories — all categories
    // -------------------------------------------------------
    @Test
    @Order(5)
    @DisplayName("GET /products/categories - should return list of categories")
    public void getCategories_shouldReturnNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get(PRODUCTS_ENDPOINT + "/categories")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)));
    }

    // -------------------------------------------------------
    // GET /products/category/electronics — by category
    // -------------------------------------------------------
    @Test
    @Order(6)
    @DisplayName("GET /products/category/electronics - should return electronics only")
    public void getProductsByCategory_shouldReturnMatchingProducts() {
        given()
            .spec(requestSpec)
        .when()
            .get(PRODUCTS_ENDPOINT + "/category/electronics")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].category", equalToIgnoringCase("electronics"));
    }

    // -------------------------------------------------------
    // POST /products — create a product
    // -------------------------------------------------------
    @Test
    @Order(7)
    @DisplayName("POST /products - should create a product and return new ID")
    public void createProduct_shouldReturn200AndNewProduct() {
        String newProduct = """
                {
                    "title": "Test Automation Laptop",
                    "price": 999.99,
                    "description": "Created by automated test suite",
                    "image": "https://fakestoreapi.com/img/81fAn4.jpg",
                    "category": "electronics"
                }
                """;

        given()
            .spec(requestSpec)
            .body(newProduct)
        .when()
            .post(PRODUCTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("Test Automation Laptop"))
            .body("price", equalTo(999.99f));
    }

    // -------------------------------------------------------
    // PUT /products/{id} — update a product
    // -------------------------------------------------------
    @Test
    @Order(8)
    @DisplayName("PUT /products/1 - should update product title and price")
    public void updateProduct_shouldReturnUpdatedFields() {
        String updatedProduct = """
                {
                    "title": "Updated Product Title",
                    "price": 49.99,
                    "description": "Updated by test",
                    "image": "https://fakestoreapi.com/img/81fAn4.jpg",
                    "category": "jewelery"
                }
                """;

        given()
            .spec(requestSpec)
            .body(updatedProduct)
        .when()
            .put(PRODUCTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Product Title"))
            .body("price", equalTo(49.99f));
    }

    // -------------------------------------------------------
    // PATCH /products/{id} — partial update
    // -------------------------------------------------------
    @Test
    @Order(9)
    @DisplayName("PATCH /products/1 - should partially update product price")
    public void patchProduct_shouldUpdateOnlySpecifiedFields() {
        String patch = """
                {
                    "price": 19.99
                }
                """;

        given()
            .spec(requestSpec)
            .body(patch)
        .when()
            .patch(PRODUCTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("price", equalTo(19.99f));
    }

    // -------------------------------------------------------
    // DELETE /products/{id} — delete a product
    // -------------------------------------------------------
    @Test
    @Order(10)
    @DisplayName("DELETE /products/1 - should return 200 and the deleted product")
    public void deleteProduct_shouldReturn200WithDeletedProduct() {
        given()
            .spec(requestSpec)
        .when()
            .delete(PRODUCTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }
}
