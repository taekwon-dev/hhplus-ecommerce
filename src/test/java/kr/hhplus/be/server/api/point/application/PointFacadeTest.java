package kr.hhplus.be.server.api.point.application;

import kr.hhplus.be.server.api.point.application.dto.ChargeResult;
import kr.hhplus.be.server.api.point.application.dto.GetBalanceResult;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.model.PointTransaction;
import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.repository.PointTransactionRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 Facade 통합 테스트")
class PointFacadeTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PointFacade pointFacade;

    @DisplayName("유저의 포인트 잔액을 조회한다.")
    @Test
    void findPointByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when
        GetBalanceResult result = pointFacade.getBalance(user.getId());

        // then
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.balance()).isEqualTo(initialBalance);
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void addPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));
        int amountToCharge = 1_000;

        // when
        ChargeResult result = pointFacade.charge(user.getId(), amountToCharge);

        // then
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByUserId(user.getId());
        assertThat(pointTransactions.size()).isEqualTo(1);
        assertThat(pointTransactions.get(0).getTransactionType()).isEqualTo(PointTransactionType.CHARGE);
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.balance()).isEqualTo(initialBalance + amountToCharge);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 0 또는 음수인 경우 예외가 발생한다.")
    @Test
    void addPoints_invalidMinimumAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));
        int amountToCharge = 0;

        // when & then
        assertThatThrownBy(() -> pointFacade.charge(user.getId(), amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 1,000 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void addPoints_invalidAmountUnit() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));
        int amountToCharge = 1_500;

        // when & then
        assertThatThrownBy(() -> pointFacade.charge(user.getId(), amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }
}
