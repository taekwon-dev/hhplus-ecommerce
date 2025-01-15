package kr.hhplus.be.server.api.payment.facade;

import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.response.PaymentResponse;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.payment.domain.DataPlatformClient;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DataPlatformClient dataPlatformClient;

    @Transactional
    public PaymentResponse pay(User user, PaymentRequest request) {
        Order order = orderService.findById(request.orderId());
        orderService.validateOrderOwnership(user, order);

        List<ProductQuantityDto> productQuantityDtos = mapToProductQuantityDtos(request);
        productService.deductStock(productQuantityDtos);

        PaymentMethod paymentMethod = request.paymentMethod();
        int totalPrice = order.calculateTotalPrice();
        Payment payment = paymentService.pay(order, paymentMethod, totalPrice);

        orderService.completeOrder(order);
        dataPlatformClient.send(new PaymentCompletedEvent(user.getId(), order.getId(), totalPrice));
        return new PaymentResponse(order.getId(), payment.getId(), payment.getAmount(), payment.getMethod(), payment.getStatus());
    }

    private List<ProductQuantityDto> mapToProductQuantityDtos(PaymentRequest request) {
        Order order = orderService.findById(request.orderId());
        return order.getOrderProducts().stream()
                .map(orderProduct -> new ProductQuantityDto(orderProduct.getProduct().getId(), orderProduct.getQuantity()))
                .collect(Collectors.toList());
    }
}
