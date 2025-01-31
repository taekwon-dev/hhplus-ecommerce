package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.SaveOrderParam;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("결제 Service 통합 테스트")
class PaymentServiceIntegrationTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @DisplayName("결제를 한다.")
    @Test
    void pay() {
        // given
        User user = userRepository.save(UserFixture.USER());
        pointRepository.save(new Point(user.getId(), 100_000));
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order order = orderService.saveOrder(user.getId(), param);
        int paymentAmount = product.getSalesPrice() * quantity;

        // when
        Payment savedPayment = paymentService.pay(user.getId(), order.getId(), paymentAmount, PaymentMethod.POINT_PAYMENT);

        // then
        assertThat(savedPayment.getOrderId()).isEqualTo(order.getId());
        assertThat(savedPayment.getMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT);
        assertThat(savedPayment.getAmount()).isEqualTo(paymentAmount);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @DisplayName("결제 시 포인트 잔액 부족 시, 예외가 발생한다.")
    @Test
    void pay_Fail_InsufficientBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 10_000, 10));

        int quantity = 2;
        int paymentAmount = product.getSalesPrice() * quantity;
        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order order = orderService.saveOrder(user.getId(), param);

        int initialBalance = 10_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when & then
        assertThatThrownBy(() -> paymentService.pay(user.getId(), order.getId(), paymentAmount, PaymentMethod.POINT_PAYMENT))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }
}
