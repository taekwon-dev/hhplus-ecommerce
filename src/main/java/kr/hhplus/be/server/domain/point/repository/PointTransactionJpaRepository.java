package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, Long> {
}
