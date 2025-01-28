package kr.hhplus.be.server.domain.payment.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class UnsupportedPaymentMethodException extends BadRequestException {

    private static final String message = "지원하지 않는 결제 방식입니다.";

    public UnsupportedPaymentMethodException() {
        super(message);
    }
}
