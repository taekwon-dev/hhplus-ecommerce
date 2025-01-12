package kr.hhplus.be.server.unit.domain.point.service;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.repository.PointTransactionCoreRepository;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
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
class PointTransactionServiceTest {

    @Mock
    private PointTransactionCoreRepository pointTransactionCoreRepository;

    @InjectMocks
    private PointTransactionService pointTransactionService;

    @DisplayName("PointTransaction 저장 - 성공")
    @ParameterizedTest
    @CsvSource({"CHARGE", "USAGE"})
    void recordPointTransaction(PointTransactionType type) {
        // given
        User user = UserFixture.USER();
        int amount = 1_000;
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);

        when(pointTransactionCoreRepository.save(pointTransaction)).thenReturn(pointTransaction);

        // when
        PointTransaction savedPointTransaction = pointTransactionService.recordPointTransaction(user, amount, type);

        // then
        assertThat(savedPointTransaction.getUser()).isEqualTo(pointTransaction.getUser());
        assertThat(savedPointTransaction.getAmount()).isEqualTo(pointTransaction.getAmount());
        assertThat(savedPointTransaction.getTransactionType()).isEqualTo(pointTransaction.getTransactionType());

        verify(pointTransactionCoreRepository, times(1)).save(pointTransaction);
    }
}
