package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCouponStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCouponCoreRepository implements UserCouponRepository {

    private final UserCouponJpaRepository jpaRepository;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return jpaRepository.save(userCoupon);
    }

    @Override
    public List<Coupon> findAvailableCouponsByUser(User user, Pageable pageable) {
        return jpaRepository.findAvailableCouponsByUser(user, UserCouponStatus.AVAILABLE, pageable);
    }

    @Override
    public boolean existsByUserAndCoupon(User user, Coupon coupon) {
        return jpaRepository.existsByUserAndCoupon(user, coupon);
    }
}
