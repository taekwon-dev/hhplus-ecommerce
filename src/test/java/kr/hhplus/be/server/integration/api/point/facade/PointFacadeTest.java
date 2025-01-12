package kr.hhplus.be.server.integration.api.point.facade;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.request.PointDeductRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.api.point.facade.PointFacade;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.repository.PointTransactionRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PointFacadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PointFacade pointFacade;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("유저는 포인트를 조회한다 - 성공")
    @Test
    void findPointByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user, initialBalance));

        // when
        PointResponse response = pointFacade.getPointBalance(user.getId());

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isEqualTo(initialBalance);
    }

    @DisplayName("유저는 포인트를 충전한다 - 성공")
    @Test
    void addPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user, initialBalance));
        int amountToCharge = 1_000;
        PointAddRequest request = new PointAddRequest(user.getId(), amountToCharge);

        // when
        PointResponse response = pointFacade.addPoints(request);

        // then
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByUser(user);
        assertThat(pointTransactions.size()).isEqualTo(1);
        assertThat(pointTransactions.get(0).getTransactionType()).isEqualTo(PointTransactionType.CHARGE);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isEqualTo(initialBalance + amountToCharge);
    }

    @DisplayName("유저는 포인트를 사용한다 - 성공")
    @Test
    void deductPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user, initialBalance));
        int amountToDeduct = 1_000;
        PointDeductRequest request = new PointDeductRequest(user.getId(), amountToDeduct);

        // when
        PointResponse response = pointFacade.deductPoints(request);

        // then
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByUser(user);
        assertThat(pointTransactions.size()).isEqualTo(1);
        assertThat(pointTransactions.get(0).getTransactionType()).isEqualTo(PointTransactionType.USAGE);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isZero();
    }
}
