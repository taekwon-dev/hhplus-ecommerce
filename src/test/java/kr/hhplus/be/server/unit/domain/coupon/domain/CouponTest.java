package kr.hhplus.be.server.unit.domain.coupon.domain;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.exception.InvalidCouponDateException;
import kr.hhplus.be.server.domain.coupon.exception.InvalidMaxIssuableCountException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @DisplayName("Coupon 생성 - 성공")
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

    @DisplayName("Coupon 생성 - 실패 - 유효 기간 시작 날짜가 종료 날짜보다 늦음")
    @Test
    void createCoupon_Fail_InvalidCouponDate() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.minusWeeks(1);

        // when & then
        assertThatThrownBy(() -> new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 10))
                .isInstanceOf(InvalidCouponDateException.class);
    }

    @DisplayName("Coupon 생성 - 실패 - 최대 쿠폰 발급 수가 음수")
    @Test
    void createCoupon_Fail_InvalidMaxIssuableCount() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);

        // when & then
        assertThatThrownBy(() -> new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, -10))
                .isInstanceOf(InvalidMaxIssuableCountException.class);
    }

    @DisplayName("Coupon 발급 - 성공")
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

    @DisplayName("Coupon 발급 - 실패 - 최대 쿠폰 발급 수량 초과")
    @Test
    void issue_Fail_ExceededMaxIssuableCount() {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = new Coupon("1a2b3c", CouponDiscountType.RATE, 10, startDate, endDate, 0);

        // when & then
        assertThatThrownBy(coupon::issue)
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }
}
