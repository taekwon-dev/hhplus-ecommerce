package kr.hhplus.be.server.domain.coupon.service.dto;

public record CouponIssueParam(
        Long userId,
        Long couponId
) {
}
