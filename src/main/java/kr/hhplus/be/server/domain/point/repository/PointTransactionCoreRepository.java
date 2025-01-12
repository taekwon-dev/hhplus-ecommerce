package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointTransactionCoreRepository implements PointTransactionRepository {

    private final PointTransactionJpaRepository jpaRepository;

    @Override
    public PointTransaction save(PointTransaction pointTransaction) {
        return jpaRepository.save(pointTransaction);
    }

    @Override
    public List<PointTransaction> findByUser(User user) {
        return jpaRepository.findByUser(user);
    }
}
