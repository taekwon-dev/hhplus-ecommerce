package kr.hhplus.be.server.infra.storage.core.jpa.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(Long id);

    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE p.stockQuantity > 0")
    List<Product> findSellableProducts(Pageable pageable);

    @Query("""
        SELECT NEW kr.hhplus.be.server.domain.product.model.BestSellingProduct(
            p.id,
            p.name,
            p.salesPrice,
            p.stockQuantity,
            SUM(op.quantity)
        )
        FROM Product p
        JOIN OrderProduct op ON p.id = op.productId
        JOIN Order o ON o.id = op.orderId
        WHERE o.status IN (:statuses) AND o.updatedAt BETWEEN :startDateTime AND :endDateTime
        GROUP BY p.id
        ORDER BY SUM(op.quantity) DESC
    """)
    List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable, List<OrderStatus> statuses);
}
