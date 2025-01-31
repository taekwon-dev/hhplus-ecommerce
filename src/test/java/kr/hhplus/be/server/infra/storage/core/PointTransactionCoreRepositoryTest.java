package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.point.model.PointTransaction;
import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PointTransactionJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionCoreRepositoryTest {

    @Mock
    private PointTransactionJpaRepository pointTransactionJpaRepository;

    @InjectMocks
    private PointTransactionCoreRepository pointTransactionCoreRepository;

    @DisplayName("포인트 충전, 사용 이력을 저장한다.")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void savePointTransaction(PointTransactionType type) {
        // given
        User user = UserFixture.USER(1L);
        int amount = 1_000;
        PointTransaction pointTransaction = new PointTransaction(user.getId(), amount, type);

        when(pointTransactionJpaRepository.save(pointTransaction)).thenReturn(pointTransaction);

        // when
        PointTransaction savedPointTransaction = pointTransactionCoreRepository.save(pointTransaction);

        // then
        assertThat(savedPointTransaction.getUserId()).isEqualTo(pointTransaction.getUserId());
        assertThat(savedPointTransaction.getAmount()).isEqualTo(pointTransaction.getAmount());
        assertThat(savedPointTransaction.getTransactionType()).isEqualTo(pointTransaction.getTransactionType());

        verify(pointTransactionJpaRepository, times(1)).save(pointTransaction);
    }

    @DisplayName("유저의 포인트 충전, 사용 이력을 조회한다.")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void findByUser(PointTransactionType type) {
        User user = UserFixture.USER(1L);
        int amount = 1_000;
        PointTransaction pointTransaction = new PointTransaction(user.getId(), amount, type);
        List<PointTransaction> pointTransactions = List.of(pointTransaction);

        when(pointTransactionJpaRepository.findByUserId(user.getId())).thenReturn(pointTransactions);

        // when
        List<PointTransaction> foundPointTransactions = pointTransactionCoreRepository.findByUserId(user.getId());

        // then
        assertThat(foundPointTransactions).hasSize(1);
        assertThat(foundPointTransactions.get(0).getUserId()).isEqualTo(pointTransaction.getUserId());
        assertThat(foundPointTransactions.get(0).getAmount()).isEqualTo(pointTransaction.getAmount());
        assertThat(foundPointTransactions.get(0).getTransactionType()).isEqualTo(pointTransaction.getTransactionType());

        verify(pointTransactionJpaRepository, times(1)).findByUserId(user.getId());
    }
}
