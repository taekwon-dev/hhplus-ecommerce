package kr.hhplus.be.server.api.point.application;

import kr.hhplus.be.server.api.point.application.dto.ChargeResult;
import kr.hhplus.be.server.api.point.application.dto.GetBalanceResult;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointFacade {

    private final PointService pointService;
    private final PointTransactionService pointTransactionService;

    @Transactional(readOnly = true)
    public GetBalanceResult getBalance(long userId) {
        Point point = pointService.findPointByUserId(userId);
        return GetBalanceResult.from(point);
    }

    @Transactional
    public ChargeResult charge(long userId, int amount) {
        Point point = pointService.charge(userId, amount);
        pointTransactionService.recordPointTransaction(userId, amount, PointTransactionType.CHARGE);
        return ChargeResult.from(point);
    }
}
