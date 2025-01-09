package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.user.domain.User;

public interface PointRepository {

    Point save(Point point);

    Point findByUser(User user);
}
