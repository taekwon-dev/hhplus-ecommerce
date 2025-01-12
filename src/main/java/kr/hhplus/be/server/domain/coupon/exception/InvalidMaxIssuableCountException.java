package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.global.exception.BadRequestException;

public class InvalidMaxIssuableCountException extends BadRequestException {

    private static final String message = "쿠폰 최대 발급 가능 수량은 음수가 될 수 없습니다.";

    public InvalidMaxIssuableCountException() {
        super(message);
    }
}
