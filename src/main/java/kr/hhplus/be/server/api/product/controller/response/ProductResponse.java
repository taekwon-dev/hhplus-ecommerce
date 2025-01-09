package kr.hhplus.be.server.api.product.controller.response;

public record ProductResponse(
        long productId,
        String name,
        int price,
        int stockQuantity
) {
}
