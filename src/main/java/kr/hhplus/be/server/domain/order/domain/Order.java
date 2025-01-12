package kr.hhplus.be.server.domain.order.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.BaseEntity;
import kr.hhplus.be.server.domain.order.domain.product.OrderProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "`order`")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "order")
    private List<OrderProduct> orderProducts;

    public Order(Long id, User user) {
        this(user);
        this.id = id;
    }

    public Order(User user) {
        this.user = user;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.orderProducts = new ArrayList<>();
    }

    public void addOrderProduct(Product product, int quantity) {
        OrderProduct orderProduct = new OrderProduct(this, product, quantity);
        orderProducts.add(orderProduct);
    }

    public int calculateTotalPrice() {
        return orderProducts.stream()
                .mapToInt(orderProduct -> orderProduct.getProduct().getPrice() * orderProduct.getQuantity())
                .sum();
    }

    public void complete() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
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
