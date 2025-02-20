package kr.hhplus.be.server.domain.product.model;

public record BestSellingProduct(
        long productId,
        String name,
        int salesPrice,
        int stockQuantity,
        long soldQuantity
) {
}
