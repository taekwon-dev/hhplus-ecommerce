package kr.hhplus.be.server.unit.domain.payment.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.exception.ForbiddenPaymentException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentCoreRepository paymentCoreRepository;

    @InjectMocks
    private PaymentService paymentService;

    @DisplayName("Payment 결제 자격 여부 검증 - 성공")
    @Test
    void validateOrderOwnership() {
        // given
        User user1 = UserFixture.USER(1L);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        Order order = new Order(user1);
        order.addOrderProduct(product, 1);

        // when & then
        assertThatCode(() -> paymentService.validateOrderOwnership(user1, order))
                .doesNotThrowAnyException();
    }

    @DisplayName("Payment 결제 자격 여부 검증 - 실패 - 자신의 주문이 아닌 것을 결제 시도 시 예외 발생")
    @Test
    void validateOrderOwnership_Fail_NotMine() {
        // given
        User user1 = UserFixture.USER(1L);
        User user2 = UserFixture.USER(2L);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        Order order = new Order(user1);
        order.addOrderProduct(product, 1);

        // when & then
        assertThatThrownBy(() -> paymentService.validateOrderOwnership(user2, order))
                .isInstanceOf(ForbiddenPaymentException.class);
    }


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
