package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static kr.hhplus.be.server.domain.order.domain.OrderStatus.*;

@Repository
@RequiredArgsConstructor
public class ProductCoreRepository implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Product findById(Long id) {
        return jpaRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    @Override
    public Product findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).orElseThrow(ProductNotFoundException::new);
    }

    @Override
    public Page<Product> findAllProducts(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<BestSellingProduct> findBestSellingProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        return jpaRepository.findBestSellingProducts(
                startDateTime,
                endDateTime,
                pageable,
                List.of(PAYMENT_COMPLETED, DELIVERED)
        );
    }
}
