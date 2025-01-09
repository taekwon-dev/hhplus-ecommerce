package kr.hhplus.be.server.integration.domain.order.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.OrderDetailDto;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

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

        OrderDetailDto orderDetailDto = new OrderDetailDto(product, 1);
        List<OrderDetailDto> orderDetailDtos = List.of(orderDetailDto);

        // when
        Order savedOrder = orderService.order(user, orderDetailDtos);

        // then
        assertThat(savedOrder.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(savedOrder.getOrderProducts()).hasSize(1);
    }

    @DisplayName("Order ID 기반 조회 - 성공")
    @Test
    void findById() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        OrderDetailDto orderDetailDto = new OrderDetailDto(product, 1);
        List<OrderDetailDto> orderDetailDtos = List.of(orderDetailDto);
        Order savedOrder = orderService.order(user, orderDetailDtos);

        // when
        Order foundOrder = orderService.findById(savedOrder.getId());

        // then
        assertThat(foundOrder.getUser().getId()).isEqualTo(savedOrder.getUser().getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }
}
