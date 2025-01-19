package kr.hhplus.be.server.api.order.controller.request;

public record OrderProductDetail(
        Long productId,
        int quantity
) {
}
