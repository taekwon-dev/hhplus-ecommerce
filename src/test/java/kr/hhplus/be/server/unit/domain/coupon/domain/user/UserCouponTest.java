package kr.hhplus.be.server.unit.domain.coupon.domain.user;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCoupon;
import kr.hhplus.be.server.domain.coupon.domain.user.UserCouponStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserCouponTest {

    @DisplayName("유저에게 발급된 쿠폰 정보를 생성한다.")
    @Test
    void createUserCoupon() {
        // given
        User user = UserFixture.USER();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 10);

        // when
        UserCoupon userCoupon = new UserCoupon(user, coupon);

        // then
        assertThat(userCoupon.getUser()).isEqualTo(user);
        assertThat(userCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.AVAILABLE);
    }
}
