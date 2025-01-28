package kr.hhplus.be.server.domain.payment.service;

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
    public Payment pay(long userId, long orderId, int amount, PaymentMethod paymentMethod) {
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getPaymentStrategy(paymentMethod);
        paymentStrategy.pay(userId, amount);
        Payment payment = new Payment(orderId, amount, paymentMethod, PaymentStatus.CONFIRMED);
        return paymentRepository.save(payment);
    }
}
