package kr.hhplus.be.server.unit.api.payment.facade;

import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.response.PaymentResponse;
import kr.hhplus.be.server.domain.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.domain.*;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
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
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private DataPlatformClient dataPlatformClient;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @DisplayName("Payment - 성공")
    @Test
    void pay() {
        // given
        User user = UserFixture.USER(1L);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, price, stockQuantity);

        int quantity = 1;
        Order order = new Order(1L, user);
        order.addOrderProduct(product, 1);
        int totalPrice = order.calculateTotalPrice();

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), quantity);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        Payment payment = new Payment(1L, order, PaymentMethod.POINT_PAYMENT, totalPrice, PaymentStatus.CONFIRMED);
        PaymentRequest request = new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT);

        when(orderService.findById(order.getId())).thenReturn(order);
        doNothing().when(orderService).validateOrderOwnership(user, order);
        doNothing().when(productService).deductStock(productQuantityDtos);
        when(paymentService.pay(order, PaymentMethod.POINT_PAYMENT, totalPrice)).thenReturn(payment);
        doNothing().when(orderService).completeOrder(order);
        doNothing().when(dataPlatformClient).send(new PaymentCompletedEvent(user.getId(), order.getId(), totalPrice));

        // when
        PaymentResponse response = paymentFacade.pay(user, request);

        // then
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.paymentId()).isEqualTo(payment.getId());
        assertThat(response.paymentMethod()).isEqualTo(payment.getMethod());
        assertThat(response.totalPrice()).isEqualTo(payment.getAmount());
        assertThat(response.paymentStatus()).isEqualTo(payment.getStatus());

        verify(orderService, times(2)).findById(order.getId());
        verify(orderService, times(1)).validateOrderOwnership(user, order);
        verify(productService, times(1)).deductStock(productQuantityDtos);
        verify(paymentService, times(1)).pay(order, PaymentMethod.POINT_PAYMENT, totalPrice);
        verify(orderService, times(1)).completeOrder(order);
        verify(dataPlatformClient, times(1)).send(new PaymentCompletedEvent(user.getId(), order.getId(), totalPrice));
    }
}
