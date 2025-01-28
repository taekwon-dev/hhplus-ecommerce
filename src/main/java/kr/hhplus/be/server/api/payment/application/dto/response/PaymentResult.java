package kr.hhplus.be.server.api.payment.application.dto.response;

import kr.hhplus.be.server.domain.payment.domain.Payment;

public record PaymentResult(
        long orderId,
        long paymentId,
        int paymentAmount,
        String paymentMethod,
        String paymentStatus
) {
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getOrderId(),
                payment.getId(),
                payment.getAmount(),
                payment.getMethod().name(),
                payment.getStatus().name()
        );
    }
}
