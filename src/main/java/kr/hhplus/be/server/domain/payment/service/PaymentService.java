package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.*;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Transactional
    public Payment pay(Order order, PaymentMethod paymentMethod, int totalPrice) {
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getPaymentStrategy(paymentMethod);
        paymentStrategy.pay(order.getUser(), totalPrice);
        Payment payment = new Payment(order, paymentMethod, totalPrice, PaymentStatus.CONFIRMED);
        return paymentRepository.save(payment);
    }
}
