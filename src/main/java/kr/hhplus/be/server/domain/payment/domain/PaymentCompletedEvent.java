package kr.hhplus.be.server.domain.payment.domain;

public record PaymentCompletedEvent(
        Long userId,
        Long orderId,
        int totalPrice
) {
}