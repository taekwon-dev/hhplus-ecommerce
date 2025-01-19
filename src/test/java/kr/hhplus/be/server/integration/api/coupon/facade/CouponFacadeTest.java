package kr.hhplus.be.server.integration.api.coupon.facade;

import kr.hhplus.be.server.api.coupon.controller.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.controller.response.CouponResponse;
import kr.hhplus.be.server.api.coupon.facade.CouponFacade;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CouponFacadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponFacade couponFacade;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("사용 가능한 보유 Coupon 목록 조회 - 성공")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user, coupon.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<CouponResponse> responses = couponFacade.findAvailableCoupons(user, pageable);

        // then
        assertThat(responses).hasSize(1);
    }

    @DisplayName("Coupon 발급 - 성공")
    @Test
    void issue() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when
        CouponResponse response = couponFacade.issue(user, request);

        // then
        assertThat(response.couponId()).isEqualTo(coupon.getId());
        assertThat(response.code()).isEqualTo(coupon.getCode());
    }

    @DisplayName("Coupon 발급 - 실패 - 최대 쿠폰 발급 수량 초과")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 0));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        // when & then
        assertThatThrownBy(() -> couponFacade.issue(user, request))
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }

    @DisplayName("Coupon 발급 - 실패 - 이미 발급 받은 쿠폰")
    @Test
    void issue_alreadyIssuedCoupon() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());
        couponFacade.issue(user, request);

        // when & then
        assertThatThrownBy(() -> couponFacade.issue(user, request))
                .isInstanceOf(AlreadyIssuedCouponException.class);
    }
}
