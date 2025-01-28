package kr.hhplus.be.server.infra.storage.core.jpa.repository;

import kr.hhplus.be.server.domain.order.model.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long> {
}
