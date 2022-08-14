package me.blessedsibanda.microservices.composite.product.services;

import me.blessedsibanda.api.composite.product.*;
import me.blessedsibanda.api.core.product.Product;
import me.blessedsibanda.api.core.recommendation.Recommendation;
import me.blessedsibanda.api.core.review.Review;
import me.blessedsibanda.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    public ProductCompositeServiceImpl(ServiceUtil serviceUtil,
                                       ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregrate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        List<Recommendation> recommendations =
                integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);
        return createProductAggregate(product, recommendations,
                reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregrate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress
    ) {
        // Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null :
                        recommendations.stream()
                                .map(r ->
                                        new RecommendationSummary(
                                                r.getRecommendationId(),
                                                r.getAuthor(),
                                                r.getRate()
                                        )).toList();

        // Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(
                                r.getReviewId(),
                                r.getAuthor(),
                                r.getSubject()
                        )).toList();

        // Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0)
                ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null
                && recommendations.size() > 0) ?
                recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(
                serviceAddress,
                productAddress,
                reviewAddress,
                recommendationAddress
        );
        return new ProductAggregrate(productId, name, weight,
                recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
