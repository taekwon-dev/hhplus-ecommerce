package kr.hhplus.be.server.unit.domain.payment.repository;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.repository.PaymentCoreRepository;
import kr.hhplus.be.server.domain.payment.repository.PaymentJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCoreRepositoryTest {

    @Mock
    private PaymentJpaRepository paymentJpaRepository;

    @InjectMocks
    private PaymentCoreRepository paymentCoreRepository;

    @DisplayName("결제 이력을 저장한다.")
    @Test
    void savePayment() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);
        Payment payment = new Payment(order, PaymentMethod.POINT_PAYMENT, 1_000, PaymentStatus.CONFIRMED);

        when(paymentJpaRepository.save(payment)).thenReturn(payment);

        // when
        Payment savedPayment = paymentCoreRepository.save(payment);

        // then
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getMethod()).isEqualTo(payment.getMethod());
        assertThat(savedPayment.getAmount()).isEqualTo(payment.getAmount());
        assertThat(savedPayment.getStatus()).isEqualTo(payment.getStatus());

        verify(paymentJpaRepository, times(1)).save(payment);
    }
}
