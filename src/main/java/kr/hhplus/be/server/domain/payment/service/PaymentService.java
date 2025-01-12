package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.payment.exception.ForbiddenPaymentException;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void validateOrderOwnership(User user, Order order) {
        if (!user.equals(order.getUser())) {
            throw new ForbiddenPaymentException();
        }
    }

    @Transactional
    public Payment pay(Order order, PaymentMethod paymentMethod, int totalPrice) {
        Payment payment = new Payment(order, paymentMethod, totalPrice, PaymentStatus.CONFIRMED);
        return paymentRepository.save(payment);
    }
}
