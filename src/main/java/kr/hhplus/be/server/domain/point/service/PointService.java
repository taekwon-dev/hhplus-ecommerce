package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Point findPointByUser(User user) {
        return pointRepository.findByUser(user);
    }

    @Transactional
    public Point addPoints(User user, int amount) {
        Point point = pointRepository.findByUserWithLock(user);
        point.add(amount);
        return pointRepository.save(point);
    }

    @Transactional
    public Point deductPoints(User user, int amount) {
        Point point = pointRepository.findByUserWithLock(user);
        point.deduct(amount);
        return pointRepository.save(point);
    }
}
