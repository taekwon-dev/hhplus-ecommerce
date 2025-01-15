package kr.hhplus.be.server.unit.api.order.facade;

import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.controller.request.OrderProductDetail;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.OrderDetailDto;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderFacade orderFacade;

    @DisplayName("Order - 성공")
    @Test
    void order() {
        // given
        User user = UserFixture.USER(1L);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        long productId = 1L;
        Product product = new Product(productId, name, category, price, stockQuantity);

        int quantity = 5;
        ProductQuantityDto productQuantityDto = new ProductQuantityDto(productId, quantity);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        OrderDetailDto orderDetailDto = new OrderDetailDto(product, quantity);
        List<OrderDetailDto> orderDetailDtos = List.of(orderDetailDto);

        Order expectedOrder = new Order(1L, user);
        expectedOrder.addOrderProduct(product, quantity);

        OrderProductDetail orderProductDetail = new OrderProductDetail(product.getId(), quantity);
        List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
        OrderCreateRequest request = new OrderCreateRequest(orderProductDetails);
        
        doNothing().when(productService).validateStock(productQuantityDtos);
        when(productService.findById(productId)).thenReturn(product);
        when(orderService.order(user, orderDetailDtos)).thenReturn(expectedOrder);

        // when
        long savedOrderId = orderFacade.order(user, request);

        // then
        assertThat(savedOrderId).isEqualTo(expectedOrder.getId());
        
        verify(productService, times(1)).validateStock(productQuantityDtos);
        verify(productService, times(1)).findById(productId);
        verify(orderService, times(1)).order(user, orderDetailDtos);
    }
}
