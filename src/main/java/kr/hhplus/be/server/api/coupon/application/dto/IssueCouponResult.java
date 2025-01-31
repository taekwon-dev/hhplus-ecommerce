package kr.hhplus.be.server.api.coupon.application.dto;

import kr.hhplus.be.server.domain.coupon.model.Coupon;

import java.time.LocalDateTime;

public record IssueCouponResult(
        long couponId,
        String code,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static IssueCouponResult from(Coupon coupon) {
        return new IssueCouponResult(
                coupon.getId(),
                coupon.getCode(),
                coupon.getStartDate(),
                coupon.getEndDate()
        );
    }
}
