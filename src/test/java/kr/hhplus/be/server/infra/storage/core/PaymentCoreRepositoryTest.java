package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PaymentJpaRepository;
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
        User user = UserFixture.USER(1L);
        Order order = new Order(1L, user.getId());
        Payment payment = new Payment(order.getId(), 1_000, PaymentMethod.POINT_PAYMENT, PaymentStatus.CONFIRMED);

        when(paymentJpaRepository.save(payment)).thenReturn(payment);

        // when
        Payment savedPayment = paymentCoreRepository.save(payment);

        // then
        assertThat(savedPayment.getOrderId()).isEqualTo(order.getId());
        assertThat(savedPayment.getMethod()).isEqualTo(payment.getMethod());
        assertThat(savedPayment.getAmount()).isEqualTo(payment.getAmount());
        assertThat(savedPayment.getStatus()).isEqualTo(payment.getStatus());

        verify(paymentJpaRepository, times(1)).save(payment);
    }
}
