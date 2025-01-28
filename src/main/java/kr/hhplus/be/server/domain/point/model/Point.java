package kr.hhplus.be.server.domain.point.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseEntity;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointDeductionAmountException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    public Point(Long id, Long userId, int balance) {
        this(userId, balance);
        this.id = id;
    }

    public Point(Long userId, int balance) {
        validateBalance(balance);
        this.userId = userId;
        this.balance = balance;
    }

    private void validateBalance(int balance) {
        if (balance < 0) {
            throw new InvalidPointBalanceException();
        }
    }

    public void charge(int amount) {
        validateAddition(amount);
        this.balance += amount;
    }

    private void validateAddition(int amount) {
        if (amount <= 0 || amount % 1_000 != 0) {
            throw new InvalidPointAdditionAmountException();
        }
    }

    public void deduct(int amount) {
        validateDeduction(amount);
        this.balance -= amount;
    }

    private void validateDeduction(int amount) {
        if (amount <= 0) {
            throw new InvalidPointDeductionAmountException();
        }

        if (this.balance < amount) {
            throw new InsufficientPointBalanceException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point point)) {
            return false;
        }
        return Objects.equals(id, point.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
