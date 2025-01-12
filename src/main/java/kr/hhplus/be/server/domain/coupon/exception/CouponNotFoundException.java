package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.global.exception.NotFoundException;

public class CouponNotFoundException extends NotFoundException {

    private static final String message = "해당 쿠폰을 찾을 수 없습니다.";

    public CouponNotFoundException() {
        super(message);
    }
}
