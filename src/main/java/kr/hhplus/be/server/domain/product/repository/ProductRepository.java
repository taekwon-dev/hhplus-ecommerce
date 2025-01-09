package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Product;

import java.util.List;

public interface ProductRepository {

    Product save(Product product);

    Product findById(Long id);

    Product findByIdWithLock(Long id);
}
