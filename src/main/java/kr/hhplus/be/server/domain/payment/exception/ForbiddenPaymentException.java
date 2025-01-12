package kr.hhplus.be.server.domain.payment.exception;

import kr.hhplus.be.server.global.exception.ForbiddenException;

public class ForbiddenPaymentException extends ForbiddenException {

    private static final String message = "해당 주문에 대한 결제 권한이 없습니다.";

    public ForbiddenPaymentException() {
        super(message);
    }
}
