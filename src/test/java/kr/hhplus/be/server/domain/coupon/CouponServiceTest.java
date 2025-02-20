package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.infra.storage.core.CouponCoreRepository;
import kr.hhplus.be.server.infra.storage.core.IssuedCouponCoreRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("쿠폰 Service 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    
    @Mock
    private CouponCoreRepository couponCoreRepository;
    
    @Mock
    private IssuedCouponCoreRepository issuedCouponCoreRepository;

    @InjectMocks
    private CouponService couponService;

    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(issuedCouponCoreRepository.findAvailableCouponsByUserId(user.getId(), pageable)).thenReturn(List.of(coupon));

        // when
        List<Coupon> coupons = couponService.findAvailableCoupons(user.getId(), pageable);

        // then
        assertThat(coupons.size()).isOne();
        assertThat(coupons.get(0).getDiscountType()).isEqualTo(CouponDiscountType.RATE);
        assertThat(coupons.get(0).getDiscountAmount()).isEqualTo(10);
        assertThat(coupons.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(coupons.get(0).getEndDate()).isEqualTo(endDate);
        assertThat(coupons.get(0).getIssuedCount()).isZero();
        assertThat(coupons.get(0).getMaxIssuableCount()).isEqualTo(10);

        verify(issuedCouponCoreRepository, times(1)).findAvailableCouponsByUserId(user.getId(), pageable);
    }

    @DisplayName("유저에게 쿠폰을 발급한다.")
    @Test
    void issue() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        IssuedCoupon issuedCoupon = new IssuedCoupon(user.getId(), coupon);
        
        when(couponCoreRepository.findById(coupon.getId())).thenReturn(coupon);
        when(issuedCouponCoreRepository.existsByUserIdAndCoupon(user.getId(), coupon)).thenReturn(false);
        when(issuedCouponCoreRepository.save(issuedCoupon)).thenReturn(issuedCoupon);
        when(couponCoreRepository.save(coupon)).thenReturn(coupon);

        // when
        Coupon issuedCoupon1 = couponService.issue(user.getId(), coupon.getId());

        // then
        assertThat(issuedCoupon1.getIssuedCount()).isEqualTo(1);

        verify(couponCoreRepository, times(1)).findById(coupon.getId());
        verify(issuedCouponCoreRepository, times(1)).existsByUserIdAndCoupon(user.getId(), coupon);
        verify(issuedCouponCoreRepository, times(1)).save(issuedCoupon);
        verify(couponCoreRepository, times(1)).save(coupon);
    }

    @DisplayName("쿠폰 발급 시, 발급 가능 수량을 초과한 경우 예외가 발생한다.")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 0);

        when(couponCoreRepository.findById(coupon.getId())).thenReturn(coupon);

        // when & then
        assertThatThrownBy(() -> couponService.issue(user.getId(), coupon.getId()))
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }

    @DisplayName("쿠폰 발급 시, 이미 유저에게 쿠폰 발급 이력이 있는 경우 예외가 발생한다.")
    @Test
    void issue_alreadyIssuedCoupon() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);

        when(couponCoreRepository.findById(coupon.getId())).thenReturn(coupon);
        when(issuedCouponCoreRepository.existsByUserIdAndCoupon(user.getId(), coupon)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issue(user.getId(), coupon.getId()))
                .isInstanceOf(AlreadyIssuedCouponException.class);
    }
}
