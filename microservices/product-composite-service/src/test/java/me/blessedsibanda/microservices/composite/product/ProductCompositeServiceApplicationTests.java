package me.blessedsibanda.microservices.composite.product;

import me.blessedsibanda.api.composite.product.ProductAggregrate;
import me.blessedsibanda.api.composite.product.RecommendationSummary;
import me.blessedsibanda.api.composite.product.ReviewSummary;
import me.blessedsibanda.api.core.product.Product;
import me.blessedsibanda.api.core.recommendation.Recommendation;
import me.blessedsibanda.api.core.review.Review;
import me.blessedsibanda.api.exceptions.InvalidInputException;
import me.blessedsibanda.api.exceptions.NotFoundException;
import me.blessedsibanda.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        Mockito.when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Collections.singletonList(
                        new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content",
                                "mock-address")));
        Mockito.when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Collections.singletonList(
                        new Review(PRODUCT_ID_OK, 1, "author", "subject",
                                "content", "mock-address")
                ));
        Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createCompositeProduct1() {
        ProductAggregrate compositeProduct =
                new ProductAggregrate(1, "name", 1, null, null, null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregrate compositeProduct = new ProductAggregrate(
                1, "name", 1,
                Collections.singletonList(
                        new RecommendationSummary(1, "a", 1, "c")
                ),
                Collections.singletonList(
                        new ReviewSummary(1, "a", "s", "c")
                ),
                null
        );
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregrate compositeProduct =
                new ProductAggregrate(1, "name", 1,
                        Collections.singletonList(
                                new RecommendationSummary(1, "a", 1, "c")
                        ),
                        Collections.singletonList(
                                new ReviewSummary(1, "a", "s", "c")
                        ), null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
        deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);
        deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);
    }

    @Test
    void getProduct() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
                .jsonPath("$.path")
                .isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: "
                        + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path")
                .isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private void postAndVerifyProduct(ProductAggregrate compositeProduct,
                                      HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(Mono.just(compositeProduct), ProductAggregrate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productId,
                                        HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(
            int productId, HttpStatus expectedStatus
    ) {
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
}
