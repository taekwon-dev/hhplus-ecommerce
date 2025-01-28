package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class InvalidCouponDateException extends BadRequestException {

    private static final String message = "쿠폰 유효 기간의 시작 날짜가 종료 날짜보다 늦을 수 없습니다.";

    public InvalidCouponDateException() {
        super(message);
    }
}
