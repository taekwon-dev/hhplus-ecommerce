package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @DisplayName("결제 이력을 생성한다.")
    @Test
    void createPayment() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(1L, user.getId());

        // when
        Payment payment = new Payment(order.getId(), 1_000, PaymentMethod.POINT_PAYMENT, PaymentStatus.CONFIRMED);

        // then
        assertThat(payment.getOrderId()).isEqualTo(order.getId());
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT);
        assertThat(payment.getAmount()).isEqualTo(1_000);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
