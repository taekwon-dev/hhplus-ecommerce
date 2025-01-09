package kr.hhplus.be.server.unit.api.payment.facade;

import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.response.PaymentResponse;
import kr.hhplus.be.server.api.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.service.UserService;
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
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PointService pointService;

    @Mock
    private PointTransactionService pointTransactionService;

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
        int balance = 100_000;
        Point point = new Point(1L, user, balance - totalPrice);

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), quantity);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        Payment payment = new Payment(1L, order, PaymentMethod.POINT_PAYMENT, totalPrice, PaymentStatus.CONFIRMED);
        PaymentRequest paymentRequest = new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(orderService.findById(order.getId())).thenReturn(order);
        doNothing().when(paymentService).validateOrderOwnership(user, order);
        doNothing().when(productService).deductStock(productQuantityDtos);
        when(paymentService.pay(order, PaymentMethod.POINT_PAYMENT, totalPrice)).thenReturn(payment);
        when(pointService.deductPoints(user, totalPrice)).thenReturn(point);
        when(pointTransactionService.recordPointTransaction(user, totalPrice, PointTransactionType.USAGE)).thenReturn(new PointTransaction(user, totalPrice, PointTransactionType.USAGE));


        // when
        PaymentResponse response = paymentFacade.pay(user.getId(), paymentRequest);

        // then
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.paymentId()).isEqualTo(payment.getId());
        assertThat(response.paymentMethod()).isEqualTo(payment.getMethod());
        assertThat(response.totalPrice()).isEqualTo(payment.getAmount());
        assertThat(response.paymentStatus()).isEqualTo(payment.getStatus());

        verify(userService, times(1)).findUserById(user.getId());
        verify(orderService, times(2)).findById(order.getId());
        verify(paymentService, times(1)).validateOrderOwnership(user, order);
        verify(productService, times(1)).deductStock(productQuantityDtos);
        verify(paymentService, times(1)).pay(order, PaymentMethod.POINT_PAYMENT, totalPrice);
        verify(pointService, times(1)).deductPoints(user, totalPrice);
        verify(pointTransactionService, times(1)).recordPointTransaction(user, totalPrice, PointTransactionType.USAGE);
    }
}
