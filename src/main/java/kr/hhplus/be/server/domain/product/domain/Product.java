package kr.hhplus.be.server.domain.product.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private int price;

    private int stockQuantity;

    public Product(Long id, String name, Category category, int price, int stockQuantity) {
        this(name, category, price, stockQuantity);
        this.id = id;
    }

    public Product(String name, Category category, int price, int stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void deductStockQuantity(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new InsufficientStockException();
        }
        this.stockQuantity -= quantity;
    }
}
