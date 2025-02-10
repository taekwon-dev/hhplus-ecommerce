package kr.hhplus.be.server.domain.support.kafka;

public record PaymentCompletedEvent(
        Long userId,
        Long orderId,
        int totalPrice
) {
}