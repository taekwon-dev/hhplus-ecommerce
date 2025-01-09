package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);
}
