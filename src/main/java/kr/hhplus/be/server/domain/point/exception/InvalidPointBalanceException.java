package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class InvalidPointBalanceException extends BadRequestException {

    private static final String message = "포인트 잔액이 0보다 작을 수 없습니다.";

    public InvalidPointBalanceException() {
        super(message);
    }
}
