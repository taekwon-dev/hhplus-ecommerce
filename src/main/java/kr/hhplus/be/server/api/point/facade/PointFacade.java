package kr.hhplus.be.server.api.point.facade;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.request.PointDeductRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointFacade {

    private final UserService userService;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public PointResponse getPointBalance(long userId) {
        User user = userService.findUserById(userId);
        Point point = pointService.findPointByUser(user);

        return new PointResponse(user.getId(), point.getBalance());
    }

    @Transactional
    public PointResponse addPoints(PointAddRequest request) {
        User user = userService.findUserById(request.userId());
        Point point = pointService.addPoints(user, request.amount());

        return new PointResponse(user.getId(), point.getBalance());
    }

    @Transactional
    public PointResponse deductPoints(PointDeductRequest request) {
        User user = userService.findUserById(request.userId());
        Point point = pointService.deductPoints(user, request.amount());

        return new PointResponse(user.getId(), point.getBalance());
    }
}
