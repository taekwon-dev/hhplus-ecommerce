package kr.hhplus.be.server.api.coupon.controller.request;

public record CouponIssueRequest(
        Long userId,
        Long couponId
) {
}
