package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.PointTransaction;
import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PointTransactionTest {

    @DisplayName("포인트 충전, 사용 이력을 생성한다.")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void createPointTransaction(PointTransactionType type) {
        // given
        User user = UserFixture.USER(1L);
        int amount = 1_000;

        // when
        PointTransaction pointTransaction = new PointTransaction(user.getId(), amount, type);

        // then
        assertThat(pointTransaction.getUserId()).isEqualTo(user.getId());
        assertThat(pointTransaction.getAmount()).isEqualTo(amount);
        assertThat(pointTransaction.getTransactionType()).isEqualTo(type);
    }
}
