package kr.hhplus.be.server.integration.domain.payment.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.exception.ForbiddenPaymentException;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("Payment 결제 자격 여부 검증 - 성공")
    @Test
    void validateOrderOwnership() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        Order order = new Order(user);
        order.addOrderProduct(product, 1);
        orderRepository.save(order);

        // when & then
        assertThatCode(() -> paymentService.validateOrderOwnership(user, order))
                .doesNotThrowAnyException();
    }

    @DisplayName("Payment 결제 자격 여부 검증 - 실패 - 자신의 주문이 아닌 것을 결제 시도 시 예외 발생")
    @Test
    void validateOrderOwnership_Fail_NotMine() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        Order order = new Order(user);
        order.addOrderProduct(product, 1);
        orderRepository.save(order);

        User user2 = userRepository.save(UserFixture.USER());

        // when & then
        assertThatThrownBy(() -> paymentService.validateOrderOwnership(user2, order))
                .isInstanceOf(ForbiddenPaymentException.class);
    }

    @DisplayName("Payment 저장 - 성공")
    @Test
    void pay() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        Order order = new Order(user);
        order.addOrderProduct(product, 1);
        orderRepository.save(order);

        int totalPrice = order.calculateTotalPrice();

        // when
        Payment savedPayment = paymentService.pay(order, PaymentMethod.POINT_PAYMENT, totalPrice);

        // then
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT);
        assertThat(savedPayment.getAmount()).isEqualTo(totalPrice);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
