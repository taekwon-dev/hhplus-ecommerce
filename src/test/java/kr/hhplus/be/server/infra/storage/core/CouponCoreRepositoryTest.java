package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.coupon.exception.CouponNotFoundException;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.CouponJpaRepository;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponCoreRepositoryTest {

    @Mock
    private CouponJpaRepository couponJpaRepository;

    @InjectMocks
    private CouponCoreRepository couponCoreRepository;

    @DisplayName("쿠폰을 저장한다.")
    @Test
    void saveCoupon() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(couponJpaRepository.save(coupon)).thenReturn(coupon);

        // when
        Coupon savedCoupon = couponCoreRepository.save(coupon);

        // then
        assertThat(savedCoupon.getCode()).isEqualTo(coupon.getCode());
        assertThat(savedCoupon.getDiscountType()).isEqualTo(coupon.getDiscountType());
        assertThat(savedCoupon.getDiscountAmount()).isEqualTo(coupon.getDiscountAmount());
        assertThat(savedCoupon.getStartDate()).isEqualTo(coupon.getStartDate());
        assertThat(savedCoupon.getEndDate()).isEqualTo(coupon.getEndDate());
        assertThat(savedCoupon.getIssuedCount()).isZero();
        assertThat(savedCoupon.getMaxIssuableCount()).isEqualTo(coupon.getMaxIssuableCount());

        verify(couponJpaRepository, times(1)).save(coupon);
    }

    @DisplayName("[SELECT ... FOR UPDATE] ID 기반으로 쿠폰을 조회한다.")
    @Test
    void findByIdWithLock() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(couponJpaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(coupon));

        // when
        Coupon foundCoupon = couponCoreRepository.findByIdWithLock(1L);

        // then
        assertThat(foundCoupon.getCode()).isEqualTo(coupon.getCode());
        assertThat(foundCoupon.getDiscountType()).isEqualTo(coupon.getDiscountType());
        assertThat(foundCoupon.getDiscountAmount()).isEqualTo(coupon.getDiscountAmount());
        assertThat(foundCoupon.getStartDate()).isEqualTo(coupon.getStartDate());
        assertThat(foundCoupon.getEndDate()).isEqualTo(coupon.getEndDate());
        assertThat(foundCoupon.getIssuedCount()).isZero();
        assertThat(foundCoupon.getMaxIssuableCount()).isEqualTo(coupon.getMaxIssuableCount());

        verify(couponJpaRepository, times(1)).findByIdWithLock(1L);
    }

    @DisplayName("[SELECT ... FOR UPDATE] ID 기반으로 쿠폰을 조회 시, 쿠폰을 찾지 못한 경우 예외가 발생한다.")
    @Test
    void findByIdWithLock_doNotExist() {
        // given
        when(couponJpaRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponCoreRepository.findByIdWithLock(1L))
                .isInstanceOf(CouponNotFoundException.class);
    }
}
