package kr.hhplus.be.server.api.product.controller.response;

public record BestSellingProductResponse(
        long productId,
        String name,
        int price,
        int stockQuantity,
        long soldQuantity
) {
}
