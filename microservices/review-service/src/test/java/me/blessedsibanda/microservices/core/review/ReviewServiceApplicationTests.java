package me.blessedsibanda.microservices.core.review;

import me.blessedsibanda.api.core.review.Review;
import me.blessedsibanda.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTests extends MySqlTestBase {
    @Autowired
    private WebTestClient client;

    @Autowired
    private ReviewRepository repository;

    @BeforeEach
    void setUpDb() {
        repository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getReviews() {
        int productId = 1;
        assertEquals(0, repository.findByProductId(productId).size());
        postAndVerifyReview(productId, 1, HttpStatus.OK);
        postAndVerifyReview(productId, 2, HttpStatus.OK);
        postAndVerifyReview(productId, 3, HttpStatus.OK);
        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].productId").isEqualTo(productId);
    }

    @Test
    void getReviewsMissingParameter() {
        getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review");
    }

    @Test
    void duplicateError() {
        int productId=1, reviewId=1;
        assertEquals(0, repository.count());
        postAndVerifyReview(productId, reviewId, HttpStatus.OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.reviewId").isEqualTo(reviewId);
        assertEquals(1, repository.count());
        postAndVerifyReview(productId, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo(
                        "Duplicate key, Product Id: "
                                + productId + ", Review Id: " + reviewId
                );
        assertEquals(1, repository.count());
    }

    @Test
    void getReviewsInvalidParameter() {
        getAndVerifyReviewsByProductId("?productId=no-integer",
                HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        getAndVerifyReviewsByProductId(213, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyReviewsByProductId(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    @Test
    void deleteReviews() {
        int productId=1, reviewId=1;
        postAndVerifyReview(productId, reviewId, HttpStatus.OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK);
    }

    private WebTestClient.BodyContentSpec postAndVerifyReview(
            int productId, int reviewId, HttpStatus expectedStatus
    ) {
        Review review = new Review(productId, reviewId,
                "Author " + reviewId,
                "Subject " + reviewId,
                "Content " + reviewId, "SA");
        return client.post()
                .uri("/review")
                .body(Mono.just(review), Review.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(
            String productIdQuery, HttpStatus expectedStatus
    ) {
        return client.get()
                .uri("/review" + productIdQuery)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(
            int productId, HttpStatus expectedStatus
    ) {
        return getAndVerifyReviewsByProductId(
                "?productId=" + productId,
                expectedStatus);
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(
            int productId, HttpStatus expectedStatus
    ) {
        return client.delete()
                .uri("/review?productId=" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
