package kr.hhplus.be.server.domain.payment.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public Payment(Long id, Long orderId, int amount, PaymentMethod method, PaymentStatus status) {
        this(orderId, amount, method, status);
        this.id = id;
    }

    public Payment(Long orderId, int amount, PaymentMethod method, PaymentStatus status) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }
}
