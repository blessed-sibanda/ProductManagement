package me.blessedsibanda.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.blessedsibanda.api.core.recommendation.Recommendation;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAggregrate {
    private int productId;
    private String name;
    private int weight;
    private List<RecommendationSummary> recommendations;
    private List<ReviewSummary> reviews;
    private ServiceAddresses serviceAddresses;
}
