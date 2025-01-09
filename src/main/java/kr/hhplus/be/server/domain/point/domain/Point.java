package kr.hhplus.be.server.domain.point.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointDeductionAmountException;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int balance;

    public Point(Long id, User user, int balance) {
        this(user, balance);
        this.id = id;
    }

    public Point(User user, int balance) {
        validateBalance(balance);
        this.user = user;
        this.balance = balance;
    }

    private void validateBalance(int balance) {
        if (balance < 0) {
            throw new InvalidPointBalanceException();
        }
    }

    public void add(int points) {
        validateAddition(points);
        this.balance += points;
    }

    private void validateAddition(int amount) {
        if (amount <= 0 || amount % 1_000 != 0) {
            throw new InvalidPointAdditionAmountException();
        }
    }

    public void deduct(int points) {
        validateDeduction(points);
        this.balance -= points;
    }

    private void validateDeduction(int amount) {
        if (amount <= 0) {
            throw new InvalidPointDeductionAmountException();
        }

        if (this.balance < amount) {
            throw new InsufficientPointBalanceException();
        }
    }
}
