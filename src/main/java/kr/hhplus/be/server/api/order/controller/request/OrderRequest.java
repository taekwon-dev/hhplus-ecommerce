package kr.hhplus.be.server.api.order.controller.request;

public record OrderRequest(
        Long productId,
        Integer quantity
) {
}
