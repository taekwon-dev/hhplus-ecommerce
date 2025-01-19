package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public List<Coupon> findAvailableCoupons(User user, Pageable pageable) {
        return userCouponRepository.findAvailableCouponsByUser(user, pageable);
    }

    @Transactional
    public Coupon issue(User user, long couponId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId);
        coupon.issue();
        if (userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            throw new AlreadyIssuedCouponException();
        }
        userCouponRepository.save(new UserCoupon(user, coupon));
        return couponRepository.save(coupon);
    }
}
