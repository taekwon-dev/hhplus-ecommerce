package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointTransactionCoreRepository implements PointTransactionRepository {

    private final PointTransactionJpaRepository jpaRepository;

    @Override
    public PointTransaction save(PointTransaction pointTransaction) {
        return jpaRepository.save(pointTransaction);
    }
}
