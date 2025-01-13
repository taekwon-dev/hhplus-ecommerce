package kr.hhplus.be.server.unit.domain.payment.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.repository.PaymentCoreRepository;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
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
class PaymentServiceTest {

    @Mock
    private PaymentCoreRepository paymentCoreRepository;

    @InjectMocks
    private PaymentService paymentService;

    @DisplayName("Payment 저장 - 성공")
    @Test
    void pay() {
        // given
        User user = UserFixture.USER();

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        Order order = new Order(user);
        order.addOrderProduct(product, 1);
        int totalPrice = order.calculateTotalPrice();
        Payment payment = new Payment(order, PaymentMethod.POINT_PAYMENT, totalPrice, PaymentStatus.CONFIRMED);

        when(paymentCoreRepository.save(payment)).thenReturn(payment);

        // when
        Payment savedPayment = paymentService.pay(order, PaymentMethod.POINT_PAYMENT, totalPrice);

        // then
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getMethod()).isEqualTo(payment.getMethod());
        assertThat(savedPayment.getAmount()).isEqualTo(totalPrice);
        assertThat(savedPayment.getStatus()).isEqualTo(payment.getStatus());

        verify(paymentCoreRepository, times(1)).save(payment);
    }
}
