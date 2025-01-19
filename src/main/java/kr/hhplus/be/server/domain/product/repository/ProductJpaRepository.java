package kr.hhplus.be.server.domain.product.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
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

    @Query("""
        SELECT NEW kr.hhplus.be.server.domain.product.domain.BestSellingProduct(
            p.id,
            p.name,
            p.price,
            p.stockQuantity,
            SUM(op.quantity)
        )
        FROM Product p
        JOIN OrderProduct op ON p.id = op.product.id
        JOIN op.order o
        WHERE o.status IN (:statuses) AND o.updatedAt BETWEEN :startDateTime AND :endDateTime
        GROUP BY p.id
        ORDER BY SUM(op.quantity) DESC
    """)
    List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable, List<OrderStatus> statuses);
}
