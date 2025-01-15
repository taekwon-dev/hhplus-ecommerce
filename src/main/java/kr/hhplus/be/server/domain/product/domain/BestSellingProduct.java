package kr.hhplus.be.server.domain.product.domain;

public record BestSellingProduct(
        long productId,
        String name,
        int price,
        int stockQuantity,
        long soldQuantity
) {
}
