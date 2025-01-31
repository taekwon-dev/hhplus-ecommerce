package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static kr.hhplus.be.server.domain.order.model.OrderStatus.*;

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
    public Page<Product> findSellableProducts(Pageable pageable) {
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
