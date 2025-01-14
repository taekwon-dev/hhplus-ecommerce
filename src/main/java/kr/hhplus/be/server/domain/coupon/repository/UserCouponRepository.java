package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCouponStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserCouponRepository {

    UserCoupon save(UserCoupon userCoupon);

    List<Coupon> findAvailableCouponsByUser(User user, Pageable pageable);

    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
