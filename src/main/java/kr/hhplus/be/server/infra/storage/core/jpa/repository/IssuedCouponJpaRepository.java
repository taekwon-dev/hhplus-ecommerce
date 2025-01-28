package kr.hhplus.be.server.infra.storage.core.jpa.repository;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IssuedCouponJpaRepository extends JpaRepository<IssuedCoupon, Long> {

    @Query("SELECT uc.coupon FROM IssuedCoupon uc WHERE uc.userId = :userId AND uc.status = :status")
    List<Coupon> findAvailableCouponsByUserId(long userId, IssuedCouponStatus status, Pageable pageable);

    boolean existsByUserIdAndCoupon(long userId, Coupon coupon);
}
