package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.repository.PointTransactionRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointTransactionService {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public PointTransaction recordPointTransaction(User user, int amount, PointTransactionType type) {
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);
        return pointTransactionRepository.save(pointTransaction);
    }
}
