package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.exception.CouponNotFoundException;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponCoreRepository implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    @Override
    public Coupon findById(Long id) {
        return jpaRepository.findById(id).orElseThrow(CouponNotFoundException::new);
    }

    @Override
    public Coupon findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow(CouponNotFoundException::new);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }
}
