package kr.hhplus.be.server.domain.order.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.domain.product.OrderProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order")
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
}
