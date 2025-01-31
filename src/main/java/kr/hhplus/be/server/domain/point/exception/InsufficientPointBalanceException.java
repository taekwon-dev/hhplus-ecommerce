package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class InsufficientPointBalanceException extends BadRequestException {

    private static final String message = "포인트 잔액이 부족하여 포인트를 차감할 수 없습니다.";

    public InsufficientPointBalanceException() {
        super(message);
    }
}
