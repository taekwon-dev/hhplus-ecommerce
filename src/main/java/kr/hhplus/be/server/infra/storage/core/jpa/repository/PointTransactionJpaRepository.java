package kr.hhplus.be.server.infra.storage.core.jpa.repository;

import kr.hhplus.be.server.domain.point.model.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUserId(long userId);
}
