package kr.hhplus.be.server.unit.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCouponStatus;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponCoreRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponJpaRepository;
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
class UserCouponCoreRepositoryTest {

    @Mock
    private UserCouponJpaRepository userCouponJpaRepository;

    @InjectMocks
    private UserCouponCoreRepository userCouponCoreRepository;

    @DisplayName("UserCoupon을 저장한다.")
    @Test
    void saveUserCoupon() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        UserCoupon userCoupon = new UserCoupon(user, coupon);

        when(userCouponJpaRepository.save(userCoupon)).thenReturn(userCoupon);

        // when
        UserCoupon savedUserCoupon = userCouponCoreRepository.save(userCoupon);

        // then
        assertThat(savedUserCoupon.getUser()).isEqualTo(user);
        assertThat(savedUserCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(savedUserCoupon.getStatus()).isEqualTo(userCoupon.getStatus());

        verify(userCouponJpaRepository, times(1)).save(userCoupon);
    }


    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(userCouponJpaRepository.findAvailableCouponsByUser(user, UserCouponStatus.AVAILABLE, pageable)).thenReturn(List.of(coupon));

        // when
        List<Coupon> coupons = userCouponCoreRepository.findAvailableCouponsByUser(user, pageable);

        // then
        assertThat(coupons.size()).isOne();
        assertThat(coupons.get(0).getDiscountType()).isEqualTo(CouponDiscountType.RATE);
        assertThat(coupons.get(0).getDiscountAmount()).isEqualTo(10);
        assertThat(coupons.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(coupons.get(0).getEndDate()).isEqualTo(endDate);
        assertThat(coupons.get(0).getIssuedCount()).isZero();
        assertThat(coupons.get(0).getMaxIssuableCount()).isEqualTo(10);

        verify(userCouponJpaRepository, times(1)).findAvailableCouponsByUser(user, UserCouponStatus.AVAILABLE, pageable);
    }

    @DisplayName("유저에게 쿠폰 발급 이력이 없는 상태에서 쿠폰 발급 여부를 조회한다.")
    @Test
    void existsByUserAndCoupon() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(userCouponJpaRepository.existsByUserAndCoupon(user, coupon)).thenReturn(false);

        // when
        boolean result = userCouponCoreRepository.existsByUserAndCoupon(user, coupon);

        // then
        assertThat(result).isFalse();

        verify(userCouponJpaRepository, times(1)).existsByUserAndCoupon(user, coupon);
    }

    @DisplayName("유저에게 쿠폰 발급 이력이 있는 상태에서 쿠폰 발급 여부를 조회한다.")
    @Test
    void existsByUserAndCoupon_withUserCouponExists() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(userCouponJpaRepository.existsByUserAndCoupon(user, coupon)).thenReturn(true);

        // when
        boolean result = userCouponCoreRepository.existsByUserAndCoupon(user, coupon);

        // then
        assertThat(result).isTrue();

        verify(userCouponJpaRepository, times(1)).existsByUserAndCoupon(user, coupon);
    }
}
