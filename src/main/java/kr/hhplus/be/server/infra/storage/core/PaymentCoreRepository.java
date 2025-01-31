package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.payment.domain.Payment;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PaymentJpaRepository;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentCoreRepository implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }
}
