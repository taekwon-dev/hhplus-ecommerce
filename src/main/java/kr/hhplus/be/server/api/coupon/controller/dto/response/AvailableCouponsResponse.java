package kr.hhplus.be.server.api.coupon.controller.dto.response;

import kr.hhplus.be.server.api.coupon.application.dto.AvailableCouponsResult;

import java.util.List;
import java.util.stream.Collectors;

public record AvailableCouponsResponse(
        List<AvailableCouponsResult.CouponDetail> coupons
) {
    public static AvailableCouponsResponse from(AvailableCouponsResult result) {
        return new AvailableCouponsResponse(
                result.coupons().stream()
                        .map(coupon -> new AvailableCouponsResult.CouponDetail(
                                coupon.couponId(),
                                coupon.code(),
                                coupon.startDate(),
                                coupon.endDate()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
