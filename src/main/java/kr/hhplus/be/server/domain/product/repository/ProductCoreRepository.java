package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
