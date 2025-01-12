package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.global.exception.BadRequestException;

public class MaxIssuableCountExceededException extends BadRequestException {

    private static final String message = "쿠폰이 모두 소진되어 발급 받을 수 없습니다.";

    public MaxIssuableCountExceededException() {
        super(message);
    }
}
