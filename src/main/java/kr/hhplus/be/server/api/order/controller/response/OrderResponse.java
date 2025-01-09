package kr.hhplus.be.server.api.order.controller.response;

public record OrderResponse(
        long orderId,
        String orderStatus
) {
}
