package kr.hhplus.be.server.integration.domain.point.repository.fake;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FakePointJpaRepository extends JpaRepository<Point, Long> {

    @Query("SELECT p FROM Point p WHERE p.user = :user")
    Optional<Point> findPointByUserWithLock(User user);

    Optional<Point> findPointByUser(User user);
}
