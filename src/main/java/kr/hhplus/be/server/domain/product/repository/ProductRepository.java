package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findByIdWithLock(Long id);

    List<SellableProduct> findSellableProducts(Pageable pageable);

    List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
