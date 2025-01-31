package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.PointTransaction;

import java.util.List;

public interface PointTransactionRepository {

    PointTransaction save(PointTransaction pointTransaction);

    List<PointTransaction> findByUserId(long userId);
}
