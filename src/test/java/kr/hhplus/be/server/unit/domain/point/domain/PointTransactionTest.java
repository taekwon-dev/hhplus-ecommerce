package kr.hhplus.be.server.unit.domain.point.domain;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PointTransactionTest {

    @DisplayName("PointTransaction 생성 - 성공")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void createPointTransaction(PointTransactionType type) {
        // given
        User user = UserFixture.USER();
        int amount = 1_000;

        // when
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);

        // then
        assertThat(pointTransaction.getUser()).isEqualTo(user);
        assertThat(pointTransaction.getAmount()).isEqualTo(amount);
        assertThat(pointTransaction.getType()).isEqualTo(type);
    }
}
