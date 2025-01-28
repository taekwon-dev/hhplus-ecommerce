package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.SaveOrderParam;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DisplayName("주문 Service 통합 테스트")
class OrderServiceIntegrationTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @DisplayName("주문 정보를 저장한다.")
    @Test
    void order() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);

        // when
        Order savedOrder = orderService.saveOrder(user.getId(), param);

        // then
        assertThat(savedOrder.getUserId()).isEqualTo(user.getId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(savedOrder.getOrderPrice()).isEqualTo(product.getSalesPrice() * quantity);
    }

    @DisplayName("ID 기반으로 주문 정보를 조회한다.")
    @Test
    void findById() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order savedOrder = orderService.saveOrder(user.getId(), param);

        // when
        Order foundOrder = orderService.findById(savedOrder.getId());

        // then
        assertThat(foundOrder.getUserId()).isEqualTo(savedOrder.getUserId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(foundOrder.getOrderPrice()).isEqualTo(product.getSalesPrice() * quantity);
    }

    @DisplayName("주문자를 검증한다.")
    @Test
    void validateOrderOwnership() {
        // given
        User user1 = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order savedOrder = orderService.saveOrder(user1.getId(), param);

        // when & then
        assertThatCode(() -> orderService.validateOrderOwnership(user1.getId(), savedOrder))
                .doesNotThrowAnyException();
    }

    @DisplayName("주문자 검증 시, 해당 주문을 요청한 유저가 아닌 경우 예외가 발생한다.")
    @Test
    void validateOrderOwnership_notMine() {
        // given
        User user1 = userRepository.save(UserFixture.USER());
        User user2 = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order savedOrder = orderService.saveOrder(user1.getId(), param);

        // when & then
        assertThatThrownBy(() -> orderService.validateOrderOwnership(user2.getId(), savedOrder))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @DisplayName("주문 상태를 결제 완료 상태로 변경한다.")
    @Test
    void completeOrder() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);
        Order savedOrder = orderService.saveOrder(user.getId(), param);

        // when
        orderService.completeOrder(savedOrder);

        // then
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }
}
