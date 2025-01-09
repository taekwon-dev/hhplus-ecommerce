package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.user.domain.User;

import java.util.List;

public interface PointTransactionRepository {

    PointTransaction save(PointTransaction pointTransaction);

    List<PointTransaction> findByUser(User user);
}
