package kr.hhplus.be.server.unit.domain.payment.domain;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @DisplayName("Payment 생성 - 성공")
    @Test
    void createPayment() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        // when
        Payment payment = new Payment(order, PaymentMethod.POINT_PAYMENT, 1_000, PaymentStatus.CONFIRMED);

        // then
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT);
        assertThat(payment.getAmount()).isEqualTo(1_000);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
