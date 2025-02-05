package kr.hhplus.be.server.api.payment.application;

import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.api.payment.application.dto.response.PaymentResult;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
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

@DisplayName("결제 Facade 통합 테스트")
class PaymentFacadeTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PaymentFacade paymentFacade;

    @DisplayName("결제를 한다.")
    @Test
    void pay() {
        // given
        User user = userRepository.save(UserFixture.USER());
        pointRepository.save(new Point(user.getId(), 50_000));
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentParam paymentParam = new PaymentParam(savedOrderId, product.getSalesPrice() * quantity, PaymentMethod.POINT_PAYMENT);

        // when
        PaymentResult result = paymentFacade.pay(user.getId(), paymentParam);

        // then
        assertThat(result.orderId()).isEqualTo(savedOrderId);
        assertThat(result.paymentAmount()).isEqualTo(product.getSalesPrice() * quantity);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT.name());
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.CONFIRMED.name());

        Order foundOrder = orderRepository.findById(savedOrderId);
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        Product foundProduct = productRepository.findById(product.getId()).orElseThrow(ProductNotFoundException::new);
        assertThat(foundProduct.getStockQuantity()).isEqualTo(9);
    }

    @DisplayName("결제 시 포인트 잔액 부족 시, 예외가 발생한다.")
    @Test
    void pay_Fail_InsufficientBalance() {
        // given
        int initialBalance = 10_000;
        int paymentAmount = 20_000;

        User user = userRepository.save(UserFixture.USER());
        pointRepository.save(new Point(user.getId(), initialBalance));
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 10_000, 10));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 2));
        CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
        long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

        PaymentParam paymentParam = new PaymentParam(savedOrderId, paymentAmount, PaymentMethod.POINT_PAYMENT);

        // when & then
        assertThatThrownBy(() -> paymentFacade.pay(user.getId(), paymentParam))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }
}