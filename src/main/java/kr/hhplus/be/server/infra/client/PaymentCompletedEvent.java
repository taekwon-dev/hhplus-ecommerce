package kr.hhplus.be.server.infra.client;

public record PaymentCompletedEvent(
        Long userId,
        Long orderId,
        int totalPrice
) {
}