package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUser(User user);
}
