package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.repository.PointTransactionRepository;
import kr.hhplus.be.server.domain.point.model.PointTransaction;
import kr.hhplus.be.server.domain.point.model.PointTransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointTransactionService {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public PointTransaction recordPointTransaction(long userId, int amount, PointTransactionType type) {
        PointTransaction pointTransaction = new PointTransaction(userId, amount, type);
        return pointTransactionRepository.save(pointTransaction);
    }
}
