package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.model.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Point findPointByUserId(long userId) {
        return pointRepository.findByUserId(userId);
    }

    @Transactional
    public Point charge(long userId, int amount) {
        Point point = pointRepository.findByUserIdWithLock(userId);
        point.charge(amount);
        return pointRepository.save(point);
    }

    @Transactional
    public Point deduct(long userId, int amount) {
        Point point = pointRepository.findByUserIdWithLock(userId);
        point.deduct(amount);
        return pointRepository.save(point);
    }
}
