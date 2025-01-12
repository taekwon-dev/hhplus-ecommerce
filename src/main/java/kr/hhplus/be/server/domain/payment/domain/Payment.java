package kr.hhplus.be.server.domain.payment.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.domain.Order;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public Payment(Long id, Order order, PaymentMethod method, int amount, PaymentStatus status) {
        this(order, method, amount, status);
        this.id = id;
    }

    public Payment(Order order, PaymentMethod method, int amount, PaymentStatus status) {
        this.order = order;
        this.method = method;
        this.amount = amount;
        this.status = status;
    }
}
