package kr.hhplus.be.server.integration.api.order.facade;

import kr.hhplus.be.server.api.order.controller.request.OrderRequest;
import kr.hhplus.be.server.api.order.controller.response.OrderResponse;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
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

        OrderRequest request = new OrderRequest(product.getId(), 1);
        List<OrderRequest> requests = List.of(request);

        // when
        OrderResponse response = orderFacade.order(user.getId(), requests);

        // then
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING.name());
    }

    @DisplayName("Order 저장 - 실패 - 주문 시점에 재고 부족인 경우 예외 발생")
    @Test
    void order_Fail_InsufficientStockQuantity() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 1));

        OrderRequest request = new OrderRequest(product.getId(), 2);
        List<OrderRequest> requests = List.of(request);

        // when & then
        assertThatThrownBy(() -> orderFacade.order(user.getId(), requests))
                .isInstanceOf(InsufficientStockException.class);
    }
}
