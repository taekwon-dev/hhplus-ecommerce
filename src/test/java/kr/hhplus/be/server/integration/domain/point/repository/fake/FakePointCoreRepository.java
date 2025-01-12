package kr.hhplus.be.server.integration.domain.point.repository.fake;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.domain.User;

public class FakePointCoreRepository implements PointRepository {

    private final FakePointJpaRepository fakePointJpaRepository;

    public FakePointCoreRepository(FakePointJpaRepository fakePointJpaRepository) {
        this.fakePointJpaRepository = fakePointJpaRepository;
    }

    /**
     * This method is intended to verify the behavior of concurrent access
     * without implementing concurrency control.
     */
    @Override
    public Point findByUserWithLock(User user) {
        return fakePointJpaRepository.findPointByUserWithLock(user).orElse(new Point(user, 0));
    }

    @Override
    public Point findByUser(User user) {
        return fakePointJpaRepository.findPointByUser(user).orElse(new Point(user, 0));
    }

    @Override
    public Point save(Point point) {
        return fakePointJpaRepository.save(point);
    }
}
