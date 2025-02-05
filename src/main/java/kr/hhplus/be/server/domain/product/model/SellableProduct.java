package kr.hhplus.be.server.domain.product.model;

import java.time.LocalDateTime;

public record SellableProduct(
        long productId,
        String categoryName,
        String name,
        int salesPrice,
        int stockQuantity,
        LocalDateTime createdAt
) {
}
