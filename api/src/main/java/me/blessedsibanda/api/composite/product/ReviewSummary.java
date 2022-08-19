package me.blessedsibanda.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSummary {
    private int reviewId;
    private String author;
    private String subject;
    private String content;
}
