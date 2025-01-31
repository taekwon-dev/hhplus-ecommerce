package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.api.support.exception.BadRequestException;

public class AlreadyIssuedCouponException extends BadRequestException {

    private static final String message = "이미 발급 받은 쿠폰입니다.";

    public AlreadyIssuedCouponException() {
        super(message);
    }
}
