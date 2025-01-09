package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;

import java.time.LocalDateTime;

public class CouponFixture {

    public static Coupon create(Long id, CouponDiscountType discountType, int discountAmount, LocalDateTime startDate, LocalDateTime endDate, int maxIssuableCount) {
        return new Coupon(id, "code", discountType, discountAmount, startDate, endDate, maxIssuableCount);
    }
}
