package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IssuedCouponRepository {

    IssuedCoupon save(IssuedCoupon issuedCoupon);

    List<Coupon> findAvailableCouponsByUserId(long userId, Pageable pageable);

    boolean existsByUserIdAndCoupon(long userId, Coupon coupon);
}
