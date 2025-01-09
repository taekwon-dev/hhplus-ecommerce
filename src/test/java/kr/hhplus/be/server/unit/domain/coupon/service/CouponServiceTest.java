package kr.hhplus.be.server.unit.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.repository.CouponCoreRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponCoreRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    
    @Mock
    private CouponCoreRepository couponCoreRepository;
    
    @Mock
    private UserCouponCoreRepository userCouponCoreRepository;

    @InjectMocks
    private CouponService couponService;

    @DisplayName("사용 가능한 보유 Coupon 목록 조회 - 성공")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(userCouponCoreRepository.findAvailableCouponsByUser(user)).thenReturn(List.of(coupon));

        // when
        List<Coupon> coupons = couponService.findAvailableCoupons(user);

        // then
        assertThat(coupons.size()).isOne();
        assertThat(coupons.get(0).getDiscountType()).isEqualTo(CouponDiscountType.RATE);
        assertThat(coupons.get(0).getDiscountAmount()).isEqualTo(10);
        assertThat(coupons.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(coupons.get(0).getEndDate()).isEqualTo(endDate);
        assertThat(coupons.get(0).getIssuedCount()).isZero();
        assertThat(coupons.get(0).getMaxIssuableCount()).isEqualTo(10);

        verify(userCouponCoreRepository, times(1)).findAvailableCouponsByUser(user);
    }

    @DisplayName("Coupon 발급 - 성공")
    @Test
    void issue() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        UserCoupon userCoupon = new UserCoupon(user, coupon);
        
        when(couponCoreRepository.findByIdWithLock(coupon.getId())).thenReturn(coupon);
        when(userCouponCoreRepository.existsByUserAndCoupon(user, coupon)).thenReturn(false);
        when(userCouponCoreRepository.save(userCoupon)).thenReturn(userCoupon);
        when(couponCoreRepository.save(coupon)).thenReturn(coupon);

        // when
        Coupon issuedCoupon = couponService.issue(user, coupon.getId());

        // then
        assertThat(issuedCoupon.getIssuedCount()).isEqualTo(1);

        verify(couponCoreRepository, times(1)).findByIdWithLock(coupon.getId());
        verify(userCouponCoreRepository, times(1)).existsByUserAndCoupon(user, coupon);
        verify(userCouponCoreRepository, times(1)).save(userCoupon);
        verify(couponCoreRepository, times(1)).save(coupon);
    }

    @DisplayName("Coupon 발급 - 실패 - 최대 쿠폰 발급 수량 초과")
    @Test
    void issue_Fail_ExceededMaxIssuableCount() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 0);

        when(couponCoreRepository.findByIdWithLock(coupon.getId())).thenReturn(coupon);

        // when & then
        assertThatThrownBy(() -> couponService.issue(user, coupon.getId()))
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }

    @DisplayName("Coupon 발급 - 실패 - 이미 발급 받은 쿠폰")
    @Test
    void issue_Fail_AlreadyIssuedCoupon() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(couponCoreRepository.findByIdWithLock(coupon.getId())).thenReturn(coupon);
        when(userCouponCoreRepository.existsByUserAndCoupon(user, coupon)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issue(user, coupon.getId()))
                .isInstanceOf(AlreadyIssuedCouponException.class);
    }
}
