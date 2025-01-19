package kr.hhplus.be.server.domain.point.facade;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointFacade {

    private final PointService pointService;
    private final PointTransactionService pointTransactionService;

    @Transactional(readOnly = true)
    public PointResponse getPointBalance(User user) {
        Point point = pointService.findPointByUser(user);
        return new PointResponse(user.getId(), point.getBalance());
    }

    @Transactional
    public PointResponse addPoints(User user, PointAddRequest request) {
        Point point = pointService.addPoints(user, request.amount());
        pointTransactionService.recordPointTransaction(user, request.amount(), PointTransactionType.CHARGE);
        return new PointResponse(user.getId(), point.getBalance());
    }
}
