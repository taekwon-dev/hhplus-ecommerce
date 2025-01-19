package kr.hhplus.be.server.unit.api.coupon.facade;

import kr.hhplus.be.server.api.coupon.controller.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.controller.response.CouponResponse;
import kr.hhplus.be.server.domain.coupon.facade.CouponFacade;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CouponFacadeTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private CouponFacade couponFacade;

    @DisplayName("사용 가능한 보유 Coupon 목록 조회 - 성공")
    @Test
    void findAvailableCoupons() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        List<Coupon> coupons = List.of(coupon);
        Pageable pageable = PageRequest.of(0, 10);

        when(couponService.findAvailableCoupons(user, pageable)).thenReturn(coupons);

        // when
        List<CouponResponse> responses = couponFacade.findAvailableCoupons(user, pageable);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).couponId()).isEqualTo(coupon.getId());
        assertThat(responses.get(0).code()).isEqualTo(coupon.getCode());
        assertThat(responses.get(0).startDate()).isEqualTo(coupon.getStartDate());
        assertThat(responses.get(0).endDate()).isEqualTo(coupon.getEndDate());
    }

    @DisplayName("Coupon 발급 - 성공")
    @Test
    void issue() {
        // given
        User user = UserFixture.USER(1L);
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = CouponFixture.create(1L, CouponDiscountType.RATE, 10, startDate, endDate, 10);
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        when(couponService.issue(user, coupon.getId())).thenReturn(coupon);

        // when
        CouponResponse response = couponFacade.issue(user, request);

        // then
        assertThat(response.couponId()).isEqualTo(coupon.getId());
        assertThat(response.code()).isEqualTo(coupon.getCode());
        assertThat(response.startDate()).isEqualTo(coupon.getStartDate());
        assertThat(response.endDate()).isEqualTo(coupon.getEndDate());
    }
}
