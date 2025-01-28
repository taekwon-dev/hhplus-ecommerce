package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointDeductionAmountException;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {

    @DisplayName("포인트를 생성한다.")
    @Test
    void createPoint() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;

        // when
        Point point = new Point(user.getId(), balance);

        // then
        assertThat(point.getUserId()).isEqualTo(user.getId());
        assertThat(point.getBalance()).isEqualTo(balance);
    }

    @DisplayName("포인트 생성 시, 포인트 잔액이 0보다 작아지는 경우 예외가 발생한다.")
    @Test
    void createPoint_Fail_InvalidBalance() {
        // given
        User user = UserFixture.USER(1L);

        // when & then
        assertThatThrownBy(() -> new Point(user.getId(), -1_000))
                .isInstanceOf(InvalidPointBalanceException.class);
    }

    @DisplayName("포인트를 충전한다.")
    @ParameterizedTest
    @CsvSource({
            "0, 1_000, 1_000",
            "10_000, 5_000, 15_000",
            "100_000, 120_000, 220_000"
    })
    void charge(int initialBalance, int amount, int expectedBalance) {
        // given
        User user = UserFixture.USER(1L);
        Point point = new Point(user.getId(), initialBalance);

        // when
        point.charge(amount);

        // then
        assertThat(point.getBalance()).isEqualTo(expectedBalance);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 0보다 작은 경우 예외가 발생한다.")
    @Test
    void charge_negativeAmount() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 0;
        Point point = new Point(user.getId(), balance);

        // when & then
        assertThatThrownBy(() -> point.charge(-1_000))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 1,000 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void charge_InvalidAmount() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 0;
        Point point = new Point(user.getId(), balance);

        // when & then
        assertThatThrownBy(() -> point.charge(1_200))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void deduct() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        int amount = 500;
        Point point = new Point(user.getId(), balance);

        // when
        point.deduct(amount);

        // then
        assertThat(point.getBalance()).isEqualTo(balance - amount);
    }

    @DisplayName("포인트 사용 시, 요청 포인트가 0보다 작은 경우 예외가 발생한다.")
    @Test
    void deduct_negativeAmount() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        Point point = new Point(user.getId(), balance);

        // when & then
        assertThatThrownBy(() -> point.deduct(-1_000))
                .isInstanceOf(InvalidPointDeductionAmountException.class);
    }

    @DisplayName("포인트 사용 시, 포인트 잔액보다 요청 포인트가 큰 경우 예외가 발생한다.")
    @Test
    void deduct_insufficientBalance() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        int amount = 2_000;
        Point point = new Point(user.getId(), balance);

        // when & then
        assertThatThrownBy(() -> point.deduct(amount))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }
}