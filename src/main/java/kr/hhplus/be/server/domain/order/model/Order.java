package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseEntity;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Table(name = "orders")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(Long id, Long userId) {
        this(userId);
        this.id = id;
    }

    public Order(Long userId) {
        this.userId = userId;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.orderPrice = 0;
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderPrice += orderProduct.calculateTotalPrice();
    }

    public void completePayment() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }

    public void validateOwnership(Long userId) {
        if (!userId.equals(this.userId)) {
            throw new OrderNotFoundException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order order)) {
            return false;
        }
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
