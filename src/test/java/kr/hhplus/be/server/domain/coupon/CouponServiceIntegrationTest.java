package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("쿠폰 Service 통합 테스트")
class CouponServiceIntegrationTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user.getId(), coupon.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Coupon> coupons = couponService.findAvailableCoupons(user.getId(), pageable);

        // then
        assertThat(coupons).hasSize(1);
    }

    @DisplayName("유저에게 쿠폰을 발급한다.")
    @Test
    void issue() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));

        // when
        Coupon issuedCoupon = couponService.issue(user.getId(), coupon.getId());

        // then
        assertThat(issuedCoupon.getIssuedCount()).isEqualTo(1);
    }

    @DisplayName("쿠폰 발급 시, 발급 가능 수량을 초과한 경우 예외가 발생한다.")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 0));

        // when & then
        assertThatThrownBy(() -> couponService.issue(user.getId(), coupon.getId()))
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }

    @DisplayName("쿠폰 발급 시, 이미 유저에게 쿠폰 발급 이력이 있는 경우 예외가 발생한다.")
    @Test
    void issue_alreadyIssuedCoupon() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user.getId(), coupon.getId());

        // when & then
        assertThatThrownBy(() -> couponService.issue(user.getId(), coupon.getId()))
                .isInstanceOf(AlreadyIssuedCouponException.class);
    }
}
