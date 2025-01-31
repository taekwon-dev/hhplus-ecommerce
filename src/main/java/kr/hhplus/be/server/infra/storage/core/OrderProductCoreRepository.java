package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.order.repository.OrderProductRepository;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.OrderProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderProductCoreRepository implements OrderProductRepository {

    private final OrderProductJpaRepository jpaRepository;

    @Override
    public OrderProduct save(OrderProduct orderProduct) {
        return jpaRepository.save(orderProduct);
    }
}
