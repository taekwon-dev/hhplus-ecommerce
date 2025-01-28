package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;

public interface CouponRepository {

    Coupon findById(Long id);

    Coupon findByIdWithLock(Long id);

    Coupon save(Coupon coupon);
}
