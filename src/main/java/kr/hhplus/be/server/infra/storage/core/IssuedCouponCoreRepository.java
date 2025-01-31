package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCouponStatus;
import kr.hhplus.be.server.domain.coupon.repository.IssuedCouponRepository;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.IssuedCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IssuedCouponCoreRepository implements IssuedCouponRepository {

    private final IssuedCouponJpaRepository jpaRepository;

    @Override
    public IssuedCoupon save(IssuedCoupon issuedCoupon) {
        return jpaRepository.save(issuedCoupon);
    }

    @Override
    public List<Coupon> findAvailableCouponsByUserId(long userId, Pageable pageable) {
        return jpaRepository.findAvailableCouponsByUserId(userId, IssuedCouponStatus.AVAILABLE, pageable);
    }

    @Override
    public boolean existsByUserIdAndCoupon(long userId, Coupon coupon) {
        return jpaRepository.existsByUserIdAndCoupon(userId, coupon);
    }
}
