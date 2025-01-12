package kr.hhplus.be.server.api.payment.controller.response;

import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;

public record PaymentResponse(
        long orderId,
        long paymentId,
        int totalPrice,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
