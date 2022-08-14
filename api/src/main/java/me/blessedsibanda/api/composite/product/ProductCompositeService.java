package me.blessedsibanda.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json"
    )
    ProductAggregrate getProduct(@PathVariable int productId);
}