package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;

public interface PointTransactionRepository {

    PointTransaction save(PointTransaction pointTransaction);
}
