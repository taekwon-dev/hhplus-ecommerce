package kr.hhplus.be.server.domain.point.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PointTransactionType transactionType;

    public PointTransaction(User user, int amount, PointTransactionType transactionType) {
        this.user = user;
        this.amount = amount;
        this.transactionType = transactionType;
    }
}
