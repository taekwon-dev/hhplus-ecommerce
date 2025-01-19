package kr.hhplus.be.server.unit.domain.order.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import kr.hhplus.be.server.domain.order.repository.OrderCoreRepository;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.OrderDetailDto;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderCoreRepository orderCoreRepository;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("Order 저장 - 성공")
    @Test
    void order() {
        // given
        User user = UserFixture.USER();

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        Order order = new Order(user);
        Order expectedOrder = new Order(user);
        expectedOrder.addOrderProduct(product, 1);

        OrderDetailDto orderDetailDto = new OrderDetailDto(product, 1);
        List<OrderDetailDto> orderDetailDtos = List.of(orderDetailDto);

        when(orderCoreRepository.save(order)).thenReturn(expectedOrder);

        // when
        Order savedOrder = orderService.order(user, orderDetailDtos);

        // then
        assertThat(savedOrder.getUser()).isEqualTo(order.getUser());
        assertThat(savedOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(savedOrder.getOrderProducts().size()).isOne();

        verify(orderCoreRepository, times(1)).save(order);
    }

    @DisplayName("Order ID 기반 조회 - 성공")
    @Test
    void findById() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        when(orderCoreRepository.findById(1L)).thenReturn(order);

        // when
        Order savedOrder = orderService.findById(1L);

        // then
        assertThat(savedOrder.getUser()).isEqualTo(order.getUser());
        assertThat(savedOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(savedOrder.getOrderProducts().size()).isZero();

        verify(orderCoreRepository, times(1)).findById(1L);
    }

    @DisplayName("Order 소유자 검증 - 성공")
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
        assertThatCode(() -> orderService.validateOrderOwnership(user1, order))
                .doesNotThrowAnyException();
    }

    @DisplayName("Order 소유자 검증 - 실패 - 유저가 생성한 주문이 아닌 경우 예외 발생")
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
        assertThatThrownBy(() -> orderService.validateOrderOwnership(user2, order))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @DisplayName("Order 주문 완료 상태 업데이트 - 성공")
    @Test
    void completeOrder() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(user);

        // when
        orderService.completeOrder(order);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }
}
