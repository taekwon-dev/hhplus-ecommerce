package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository {

    Product save(Product product);

    Product findById(Long id);

    Product findByIdWithLock(Long id);

    Page<Product> findAllProducts(Pageable pageable);

    List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
