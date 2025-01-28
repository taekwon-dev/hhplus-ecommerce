package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository {

    Product save(Product product);

    Product findById(Long id);

    Product findByIdWithLock(Long id);

    Page<Product> findSellableProducts(Pageable pageable);

    List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
