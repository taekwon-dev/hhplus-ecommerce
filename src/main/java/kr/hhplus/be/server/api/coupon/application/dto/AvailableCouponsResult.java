package kr.hhplus.be.server.api.coupon.application.dto;

import kr.hhplus.be.server.domain.coupon.model.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AvailableCouponsResult(
        List<CouponDetail> coupons
) {
    public record CouponDetail(
            long couponId,
            String code,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
    }

    public static AvailableCouponsResult from(List<Coupon> coupons) {
        return new AvailableCouponsResult(
                coupons.stream()
                        .map(coupon -> new AvailableCouponsResult.CouponDetail(
                                coupon.getId(),
                                coupon.getCode(),
                                coupon.getStartDate(),
                                coupon.getEndDate()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
