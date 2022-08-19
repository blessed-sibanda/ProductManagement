package me.blessedsibanda.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationSummary {
    private int recommendationId;
    private String author;
    private int rate;
    private String content;
}
