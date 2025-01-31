package kr.hhplus.be.server.api.payment.application;

import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.api.payment.application.dto.response.PaymentResult;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.infra.client.DataPlatformClient;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.infra.client.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final DataPlatformClient dataPlatformClient;

    @Transactional
    public PaymentResult pay(Long userId, PaymentParam param) {
        Order order = orderService.findById(param.orderId());
        orderService.validateOrderOwnership(userId, order);

        PaymentMethod paymentMethod = param.paymentMethod();
        Payment payment = paymentService.pay(userId, order.getId(), param.paymentAmount(), paymentMethod);

        orderService.completeOrder(order);
        dataPlatformClient.send(new PaymentCompletedEvent(userId, order.getId(), param.paymentAmount()));
        return PaymentResult.from(payment);
    }
}
