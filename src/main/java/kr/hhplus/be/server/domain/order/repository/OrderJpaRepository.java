package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
