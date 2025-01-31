package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.point.repository.PointTransactionRepository;
import kr.hhplus.be.server.domain.point.model.PointTransaction;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PointTransactionJpaRepository;
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
    public List<PointTransaction> findByUserId(long userId) {
        return jpaRepository.findByUserId(userId);
    }
}
