package kr.hhplus.be.server.domain.payment.domain;

import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentStrategy {

    private final PointService pointService;
    private final PointTransactionService pointTransactionService;

    @Override
    public void pay(User user, int amount) {
        pointService.deductPoints(user, amount);
        pointTransactionService.recordPointTransaction(user, amount, PointTransactionType.USAGE);
    }
}
