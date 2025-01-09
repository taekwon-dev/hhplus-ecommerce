package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
