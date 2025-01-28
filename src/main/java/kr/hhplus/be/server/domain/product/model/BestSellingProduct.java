package kr.hhplus.be.server.domain.product.model;

public record BestSellingProduct(
        long productId,
        String name,
        int price,
        int stockQuantity,
        long soldQuantity
) {
}
