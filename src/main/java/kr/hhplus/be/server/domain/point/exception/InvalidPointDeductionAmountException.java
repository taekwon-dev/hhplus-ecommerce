package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.global.exception.BadRequestException;

public class InvalidPointDeductionAmountException extends BadRequestException {

    private static final String message = "포인트 차감 금액은 0보다 커야 합니다.";

    public InvalidPointDeductionAmountException() {
        super(message);
    }
}
