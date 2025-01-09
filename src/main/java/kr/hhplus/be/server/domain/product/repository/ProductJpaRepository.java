package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
