package kr.hhplus.be.server.unit.domain.point.domain;

import kr.hhplus.be.server.domain.point.domain.Point;
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

    @DisplayName("Point 생성 - 성공")
    @Test
    void createPoint() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;

        // when
        Point point = new Point(user, balance);

        // then
        assertThat(point.getUser()).isEqualTo(user);
        assertThat(point.getBalance()).isEqualTo(balance);
    }

    @DisplayName("Point 생성 - 실패 - 포인트 잔액이 음수인 경우 예외 발생")
    @Test
    void createPoint_Fail_InvalidBalance() {
        // given
        User user = UserFixture.USER();

        // when & then
        assertThatThrownBy(() -> new Point(user, -1_000))
                .isInstanceOf(InvalidPointBalanceException.class);
    }

    @DisplayName("Point 추가 - 성공")
    @ParameterizedTest
    @CsvSource({
            "0, 1_000, 1_000",
            "10_000, 5_000, 15_000",
            "100_000, 120_000, 220_000"
    })
    void addPoints(int initialBalance, int points, int expectedBalance) {
        // given
        User user = UserFixture.USER();
        Point point = new Point(user, initialBalance);

        // when
        point.add(points);

        // then
        assertThat(point.getBalance()).isEqualTo(expectedBalance);
    }

    @DisplayName("Point 추가 - 실패 - 추가할 포인트가 음수인 경우 예외 발생")
    @Test
    void addPoints_Fail_NegativeAmount() {
        // given
        User user = UserFixture.USER();
        int balance = 0;
        Point point = new Point(user, balance);

        // when & then
        assertThatThrownBy(() -> point.add(-1_000))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("Point 추가 - 실패 - 추가할 포인트가 1,000 단위가 아닌 경우 예외 발생")
    @Test
    void addPoints_Fail_InvalidAmount() {
        // given
        User user = UserFixture.USER();
        int balance = 0;
        Point point = new Point(user, balance);

        // when & then
        assertThatThrownBy(() -> point.add(1_200))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("Point 차감 - 성공")
    @Test
    void deductPoints() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        int amount = 500;
        Point point = new Point(user, balance);

        // when
        point.deduct(amount);

        // then
        assertThat(point.getBalance()).isEqualTo(balance - amount);
    }

    @DisplayName("Point 차감 - 실패 - 차감할 포인트가 음수인 경우 예외 발생")
    @Test
    void deductPoints_Fail_NegativeAmount() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        Point point = new Point(user, balance);

        // when & then
        assertThatThrownBy(() -> point.deduct(-1_000))
                .isInstanceOf(InvalidPointDeductionAmountException.class);
    }

    @DisplayName("Point 차감 - 실패 - 포인트 잔액보다 차감할 포인트가 큰 경우 예외 발생")
    @Test
    void deductPoints_Fail_InsufficientBalance() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        int amount = 2_000;
        Point point = new Point(user, balance);

        // when & then
        assertThatThrownBy(() -> point.deduct(amount))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }
}