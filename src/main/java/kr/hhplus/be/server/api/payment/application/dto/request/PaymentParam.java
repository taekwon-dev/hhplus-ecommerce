package kr.hhplus.be.server.api.payment.application.dto.request;

import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;

public record PaymentParam(
        Long orderId,
        Integer paymentAmount,
        PaymentMethod paymentMethod
) {
}
