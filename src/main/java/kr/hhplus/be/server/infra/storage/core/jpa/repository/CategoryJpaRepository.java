package kr.hhplus.be.server.infra.storage.core.jpa.repository;

import kr.hhplus.be.server.domain.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}
