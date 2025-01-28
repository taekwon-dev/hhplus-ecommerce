package kr.hhplus.be.server.api.payment.controller.dto.request;

import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;

public record PaymentRequest(
        Long orderId,
        Integer paymentAmount,
        PaymentMethod paymentMethod
) {
    public PaymentParam toPaymentParam() {
        return new PaymentParam(orderId, paymentAmount, paymentMethod);
    }
}
