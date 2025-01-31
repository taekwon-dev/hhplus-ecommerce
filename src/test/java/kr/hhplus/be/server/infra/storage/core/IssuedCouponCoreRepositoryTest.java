package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCouponStatus;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssuedCouponCoreRepositoryTest {

    @Mock
    private IssuedCouponJpaRepository issuedCouponJpaRepository;

    @InjectMocks
    private IssuedCouponCoreRepository issuedCouponCoreRepository;

    @DisplayName("유저에게 쿠폰 발급 시, 쿠폰 이력을 저장한다.")
    @Test
    void saveIssuedCoupon() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        IssuedCoupon issuedCoupon = new IssuedCoupon(user.getId(), coupon);

        when(issuedCouponJpaRepository.save(issuedCoupon)).thenReturn(issuedCoupon);

        // when
        IssuedCoupon savedIssuedCoupon = issuedCouponCoreRepository.save(issuedCoupon);

        // then
        assertThat(savedIssuedCoupon.getUserId()).isEqualTo(user.getId());
        assertThat(savedIssuedCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(savedIssuedCoupon.getStatus()).isEqualTo(issuedCoupon.getStatus());

        verify(issuedCouponJpaRepository, times(1)).save(issuedCoupon);
    }


    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(issuedCouponJpaRepository.findAvailableCouponsByUserId(user.getId(), IssuedCouponStatus.AVAILABLE, pageable)).thenReturn(List.of(coupon));

        // when
        List<Coupon> coupons = issuedCouponCoreRepository.findAvailableCouponsByUserId(user.getId(), pageable);

        // then
        assertThat(coupons.size()).isOne();
        assertThat(coupons.get(0).getDiscountType()).isEqualTo(CouponDiscountType.RATE);
        assertThat(coupons.get(0).getDiscountAmount()).isEqualTo(10);
        assertThat(coupons.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(coupons.get(0).getEndDate()).isEqualTo(endDate);
        assertThat(coupons.get(0).getIssuedCount()).isZero();
        assertThat(coupons.get(0).getMaxIssuableCount()).isEqualTo(10);

        verify(issuedCouponJpaRepository, times(1)).findAvailableCouponsByUserId(user.getId(), IssuedCouponStatus.AVAILABLE, pageable);
    }

    @DisplayName("유저에게 쿠폰 발급 이력이 없는 상태에서 쿠폰 발급 여부를 조회한다.")
    @Test
    void existsByUserAndCoupon() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(issuedCouponJpaRepository.existsByUserIdAndCoupon(user.getId(), coupon)).thenReturn(false);

        // when
        boolean result = issuedCouponCoreRepository.existsByUserIdAndCoupon(user.getId(), coupon);

        // then
        assertThat(result).isFalse();

        verify(issuedCouponJpaRepository, times(1)).existsByUserIdAndCoupon(user.getId(), coupon);
    }

    @DisplayName("유저에게 쿠폰 발급 이력이 있는 상태에서 쿠폰 발급 여부를 조회한다.")
    @Test
    void existsByUserAndCoupon_withIssuedCouponExists() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(issuedCouponJpaRepository.existsByUserIdAndCoupon(user.getId(), coupon)).thenReturn(true);

        // when
        boolean result = issuedCouponCoreRepository.existsByUserIdAndCoupon(user.getId(), coupon);

        // then
        assertThat(result).isTrue();

        verify(issuedCouponJpaRepository, times(1)).existsByUserIdAndCoupon(user.getId(), coupon);
    }
}
