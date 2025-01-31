package kr.hhplus.be.server.api.payment.controller.dto.response;

import kr.hhplus.be.server.api.payment.application.dto.response.PaymentResult;

public record PaymentResponse(
        long orderId,
        long paymentId,
        int paymentAmount,
        String paymentMethod,
        String paymentStatus
) {
    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
                result.orderId(),
                result.paymentId(),
                result.paymentAmount(),
                result.paymentMethod(),
                result.paymentStatus()
        );
    }
}
