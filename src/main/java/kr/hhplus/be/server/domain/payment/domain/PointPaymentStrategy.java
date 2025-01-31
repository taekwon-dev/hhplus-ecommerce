package kr.hhplus.be.server.domain.payment.domain;

import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentStrategy {

    private final PointService pointService;
    private final PointTransactionService pointTransactionService;

    @Override
    public void pay(long userId, int amount) {
        pointService.deduct(userId, amount);
        pointTransactionService.recordPointTransaction(userId, amount, PointTransactionType.USAGE);
    }
}
