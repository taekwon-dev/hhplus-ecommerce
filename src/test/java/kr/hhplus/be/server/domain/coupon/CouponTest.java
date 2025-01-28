package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.exception.InvalidCouponDateException;
import kr.hhplus.be.server.domain.coupon.exception.InvalidMaxIssuableCountException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @DisplayName("쿠폰을 생성한다.")
    @Test
    void createCoupon() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);

        // when
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 10);

        // then
        assertThat(coupon.getCode()).isEqualTo("1a2b3c");
        assertThat(coupon.getDiscountType()).isEqualTo(CouponDiscountType.RATE);
        assertThat(coupon.getDiscountAmount()).isEqualTo(10);
        assertThat(coupon.getStartDate()).isEqualTo(startDate);
        assertThat(coupon.getEndDate()).isEqualTo(endDate);
        assertThat(coupon.getIssuedCount()).isZero();
        assertThat(coupon.getMaxIssuableCount()).isEqualTo(10);
    }

    @DisplayName("쿠폰 생성 시, 유효 기간 시작 날짜가 종료 날짜보다 늦으면 예외가 발생한다.")
    @Test
    void createCoupon_invalidCouponDate() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.minusWeeks(1);

        // when & then
        assertThatThrownBy(() -> new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 10))
                .isInstanceOf(InvalidCouponDateException.class);
    }

    @DisplayName("쿠폰 생성 시, 쿠폰 발급 가능 수량이 0보다 작은 경우 예외가 발생한다.")
    @Test
    void createCoupon_invalidMaxIssuableCount() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);

        // when & then
        assertThatThrownBy(() -> new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, -10))
                .isInstanceOf(InvalidMaxIssuableCountException.class);
    }

    @DisplayName("쿠폰 발급 시, 발급된 쿠폰 수량이 1 증가한다.")
    @Test
    void issue() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 1);

        // when
        coupon.issue();

        // then
        assertThat(coupon.getIssuedCount()).isOne();
    }

    @DisplayName("쿠폰 발급 시, 발급 가능 수량을 초과한 경우 예외가 발생한다.")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 0);

        // when & then
        assertThatThrownBy(coupon::issue)
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }
}
