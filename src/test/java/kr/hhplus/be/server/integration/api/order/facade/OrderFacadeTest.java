package kr.hhplus.be.server.integration.api.order.facade;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.controller.request.OrderProductDetail;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderFacadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("Order 저장 - 성공")
    @Test
    void order() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        OrderProductDetail orderProductDetail = new OrderProductDetail(product.getId(), 1);
        List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
        OrderCreateRequest request = new OrderCreateRequest(orderProductDetails);

        // when
        long savedOrderId = orderFacade.order(user, request);

        // then
        Order foundOrder = entityManager.createQuery(
                        "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.orderProducts WHERE o.id = :id", Order.class)
                .setParameter("id", savedOrderId)
                .getSingleResult();

        assertThat(foundOrder.getUser()).isEqualTo(user);
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(foundOrder.getOrderProducts()).hasSize(1);
    }

    @DisplayName("Order 저장 - 실패 - 주문 시점에 재고 부족인 경우 예외 발생")
    @Test
    void order_Fail_InsufficientStockQuantity() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 1));

        OrderProductDetail orderProductDetail = new OrderProductDetail(product.getId(), 2);
        List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
        OrderCreateRequest request = new OrderCreateRequest(orderProductDetails);

        // when & then
        assertThatThrownBy(() -> orderFacade.order(user, request))
                .isInstanceOf(InsufficientStockException.class);
    }
}
