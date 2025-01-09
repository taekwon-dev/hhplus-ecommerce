package kr.hhplus.be.server.api.payment.controller.request;

import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;

public record PaymentRequest(
        Long orderId,
        PaymentMethod paymentMethod
) {
}
