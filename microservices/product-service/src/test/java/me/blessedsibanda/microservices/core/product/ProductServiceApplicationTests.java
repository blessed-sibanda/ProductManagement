package me.blessedsibanda.microservices.core.product;

import me.blessedsibanda.api.core.product.Product;
import me.blessedsibanda.microservices.core.product.persistence.ProductEntity;
import me.blessedsibanda.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase {
    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUpDb() {
        repository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getProductById() {
        int productId = 1;
        postAndVerifyProduct(productId, HttpStatus.OK);
        Assertions.assertTrue(repository.findByProductId(productId).isPresent());
        getAndVerifyProduct(productId, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path")
                .isEqualTo("/product/no-integer")
                .jsonPath("$.message")
                .isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {
        int productIdNotFound = 13;
        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.message")
                        .isEqualTo("No product found for productId: "
                                + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    @Test
    void duplicateError() {
        int productId = 1;
        postAndVerifyProduct(productId, HttpStatus.OK);
        Assertions.assertTrue(repository.findByProductId(productId).isPresent());
        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: "
                        + productId);
    }

    @Test
    void deleteProduct() {
        int productId = 1;
        postAndVerifyProduct(productId, HttpStatus.OK);
        Assertions.assertTrue(repository.findByProductId(productId).isPresent());

        deleteAndVerify(productId, HttpStatus.OK);
        Assertions.assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerify(productId, HttpStatus.OK);
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(
            int productId, HttpStatus expectedStatus
    ) {
        Product product = new Product(productId, "Name " + productId,
                productId, "SA");
        return client.post()
                .uri("/product")
                .body(Mono.just(product), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(
            String productIdPath, HttpStatus expectedStatus
    ) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(
            int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec deleteAndVerify(
            int productId, HttpStatus expectedStatus
    ) {
        return client.delete()
                .uri("/product/"+productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
