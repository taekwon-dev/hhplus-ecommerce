package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PointJpaRepository;
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
    public Point findByUserId(long userId) {
        return jpaRepository.findPointByUserId(userId).orElse(new Point(userId, 0));
    }

    @Override
    public Point findByUserIdWithLock(long userId) {
        return jpaRepository.findPointByUserIdWithLock(userId).orElse(new Point(userId, 0));
    }
}
