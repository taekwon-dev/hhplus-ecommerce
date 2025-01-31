package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.Point;

public interface PointRepository {

    Point save(Point point);

    Point findByUserId(long userId);

    Point findByUserIdWithLock(long userId);
}
