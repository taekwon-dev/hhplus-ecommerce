package kr.hhplus.be.server.api.coupon.controller.response;

import java.time.LocalDateTime;

public record CouponResponse(
        long couponId,
        String code,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
