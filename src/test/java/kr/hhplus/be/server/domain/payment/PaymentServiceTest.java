package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.payment.domain.*;
import kr.hhplus.be.server.infra.storage.core.PaymentCoreRepository;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
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

@DisplayName("결제 Service 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentCoreRepository paymentCoreRepository;

    @Mock
    private PaymentStrategyFactory paymentStrategyFactory;

    @Mock
    private PointPaymentStrategy pointPaymentStrategy;

    @InjectMocks
    private PaymentService paymentService;

    @DisplayName("결제를 한다.")
    @Test
    void pay() {
        // given
        User user = UserFixture.USER(1L);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        Order order = new Order(1L, user.getId());
        int quantity = 1;
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), product.getSalesPrice(), quantity);
        order.addOrderProduct(orderProduct);
        int paymentAmount = product.getSalesPrice() * quantity;
        Payment payment = new Payment(order.getId(), paymentAmount, PaymentMethod.POINT_PAYMENT, PaymentStatus.CONFIRMED);

        when(paymentCoreRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentStrategyFactory.getPaymentStrategy(payment.getMethod())).thenReturn(pointPaymentStrategy);
        doNothing().when(pointPaymentStrategy).pay(user.getId(), paymentAmount);

        // when
        Payment savedPayment = paymentService.pay(order.getUserId(), order.getId(), paymentAmount, PaymentMethod.POINT_PAYMENT);

        // then
        assertThat(savedPayment.getOrderId()).isEqualTo(order.getId());
        assertThat(savedPayment.getMethod()).isEqualTo(payment.getMethod());
        assertThat(savedPayment.getAmount()).isEqualTo(paymentAmount);
        assertThat(savedPayment.getStatus()).isEqualTo(payment.getStatus());

        verify(paymentCoreRepository, times(1)).save(any(Payment.class));
        verify(paymentStrategyFactory, times(1)).getPaymentStrategy(payment.getMethod());
        verify(pointPaymentStrategy, times(1)).pay(user.getId(), paymentAmount);
    }
}
