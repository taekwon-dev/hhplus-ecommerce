package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {

    @Query("SELECT uc.coupon FROM UserCoupon uc WHERE uc.user = :user")
    List<Coupon> findAvailableCouponsByUser(User user);

    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
