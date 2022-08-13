package me.blessedsibanda.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.blessedsibanda.api.core.recommendation.Recommendation;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductAggregrate {
    private final int productId;
    private final String name;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;
}
