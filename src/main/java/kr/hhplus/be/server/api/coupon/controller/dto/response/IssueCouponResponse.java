package kr.hhplus.be.server.api.coupon.controller.dto.response;

import kr.hhplus.be.server.api.coupon.application.dto.IssueCouponResult;

import java.time.LocalDateTime;

public record IssueCouponResponse(
        long couponId,
        String code,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static IssueCouponResponse from(IssueCouponResult result) {
        return new IssueCouponResponse(
                result.couponId(),
                result.code(),
                result.startDate(),
                result.endDate()
        );
    }
}
