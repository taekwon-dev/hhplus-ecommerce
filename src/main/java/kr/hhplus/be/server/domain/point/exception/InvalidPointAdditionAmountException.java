package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class InvalidPointAdditionAmountException extends BadRequestException {

    private static final String message = "충전 금액은 최소 1,000원 이상이며, 1,000원 단위여야 합니다.";

    public InvalidPointAdditionAmountException() {
        super(message);
    }
}
