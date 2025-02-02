package kr.hhplus.be.server.api.coupon.application;

import kr.hhplus.be.server.api.coupon.application.dto.AvailableCouponsResult;
import kr.hhplus.be.server.api.coupon.application.dto.IssueCouponResult;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;

    @Transactional(readOnly = true)
    public AvailableCouponsResult findAvailableCoupons(long userId, Pageable pageable) {
        List<Coupon> coupons = couponService.findAvailableCoupons(userId, pageable);
        return AvailableCouponsResult.from(coupons);
    }

    @Transactional
    public IssueCouponResult issue(long userId, Long couponId) {
        Coupon coupon = couponService.issue(userId, couponId);
        return IssueCouponResult.from(coupon);
    }
}
