package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointCoreRepository implements PointRepository {

    private final PointJpaRepository jpaRepository;

    @Override
    public Point save(Point point) {
        return jpaRepository.save(point);
    }

    @Override
    public Point findByUser(User user) {
        return jpaRepository.findPointByUser(user).orElse(new Point(user, 0));
    }

    @Override
    public Point findByUserWithLock(User user) {
        return jpaRepository.findPointByUserWithLock(user).orElse(new Point(user, 0));
    }
}
