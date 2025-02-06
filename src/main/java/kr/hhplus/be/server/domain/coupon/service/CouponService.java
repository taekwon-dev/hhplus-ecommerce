package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;

    @Transactional(readOnly = true)
    public List<Coupon> findAvailableCoupons(long userId, Pageable pageable) {
        return issuedCouponRepository.findAvailableCouponsByUserId(userId, pageable);
    }

    @Transactional
    public Coupon issue(long userId, long couponId) {
        Coupon coupon = couponRepository.findById(couponId);
        coupon.issue();
        if (issuedCouponRepository.existsByUserIdAndCoupon(userId, coupon)) {
            throw new AlreadyIssuedCouponException();
        }
        issuedCouponRepository.save(new IssuedCoupon(userId, coupon));
        return couponRepository.save(coupon);
    }
}
