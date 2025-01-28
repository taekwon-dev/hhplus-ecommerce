package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.model.IssuedCoupon;
import kr.hhplus.be.server.domain.coupon.model.IssuedCouponStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IssuedCouponTest {

    @DisplayName("유저에게 발급된 쿠폰 정보를 생성한다.")
    @Test
    void createIssuedCoupon() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 10);

        // when
        IssuedCoupon issuedCoupon = new IssuedCoupon(user.getId(), coupon);

        // then
        assertThat(issuedCoupon.getUserId()).isEqualTo(user.getId());
        assertThat(issuedCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCouponStatus.AVAILABLE);
    }
}
