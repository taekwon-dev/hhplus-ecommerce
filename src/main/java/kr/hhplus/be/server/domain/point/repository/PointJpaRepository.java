package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {

    Optional<Point> findPointByUser(User user);
}
