package kr.hhplus.be.server.unit.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.repository.PointTransactionCoreRepository;
import kr.hhplus.be.server.domain.point.repository.PointTransactionJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionCoreRepositoryTest {

    @Mock
    private PointTransactionJpaRepository pointTransactionJpaRepository;

    @InjectMocks
    private PointTransactionCoreRepository pointTransactionCoreRepository;

    @DisplayName("PointTransaction 저장 - 성공")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void savePointTransaction(PointTransactionType type) {
        // given
        User user = UserFixture.USER();
        int amount = 1_000;
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);

        when(pointTransactionJpaRepository.save(pointTransaction)).thenReturn(pointTransaction);

        // when
        PointTransaction savedPointTransaction = pointTransactionCoreRepository.save(pointTransaction);

        // then
        assertThat(savedPointTransaction.getUser()).isEqualTo(pointTransaction.getUser());
        assertThat(savedPointTransaction.getAmount()).isEqualTo(pointTransaction.getAmount());
        assertThat(savedPointTransaction.getType()).isEqualTo(pointTransaction.getType());

        verify(pointTransactionJpaRepository, times(1)).save(pointTransaction);
    }
}
