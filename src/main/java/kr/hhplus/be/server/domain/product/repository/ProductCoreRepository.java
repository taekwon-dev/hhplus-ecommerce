package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public List<Product> findAllProducts() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Product> findTopSellingProducts() {
        return jpaRepository.findTopSellingProducts(List.of(PAYMENT_COMPLETED, DELIVERED));
    }
}
