package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.order.service.dto.SaveOrderParam;
import kr.hhplus.be.server.infra.storage.core.OrderCoreRepository;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.infra.storage.core.OrderProductCoreRepository;
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
import static org.mockito.Mockito.*;

@DisplayName("주문 Service 단위 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderCoreRepository orderCoreRepository;

    @Mock
    private OrderProductCoreRepository orderProductCoreRepository;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("주문 정보를 저장한다.")
    @Test
    void saveOrder() {
        // given
        User user = UserFixture.USER(1L);
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, salesPrice, stockQuantity);

        Order order = new Order(1L, user.getId());
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), product.getSalesPrice(), 1);

        List<SaveOrderParam.Detail> saveOrderParamDetails = List.of(new SaveOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
        SaveOrderParam param = new SaveOrderParam(saveOrderParamDetails);

        when(orderCoreRepository.save(any(Order.class))).thenReturn(order);
        when(orderProductCoreRepository.save(any(OrderProduct.class))).thenReturn(orderProduct);

        // when
        Order savedOrder = orderService.saveOrder(user.getId(), param);

        // then
        assertThat(savedOrder.getUserId()).isEqualTo(order.getUserId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(savedOrder.getOrderPrice()).isEqualTo(product.getSalesPrice());

        verify(orderCoreRepository, times(1)).save(any(Order.class));
        verify(orderProductCoreRepository, times(1)).save(any(OrderProduct.class));
    }

    @DisplayName("ID 기반으로 주문 정보를 조회한다.")
    @Test
    void findById() {
        // given
        User user = UserFixture.USER(1L);
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, salesPrice, stockQuantity);

        Order order = new Order(1L, user.getId());
        int quantity = 1;
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), product.getSalesPrice(), quantity);
        order.addOrderProduct(orderProduct);

        when(orderCoreRepository.findById(1L)).thenReturn(order);

        // when
        Order savedOrder = orderService.findById(1L);

        // then
        assertThat(savedOrder.getUserId()).isEqualTo(order.getUserId());
        assertThat(savedOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(savedOrder.getOrderPrice()).isEqualTo(product.getSalesPrice() * quantity);

        verify(orderCoreRepository, times(1)).findById(1L);
    }
}
