package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderProduct;

public interface OrderProductRepository {

    OrderProduct save(OrderProduct orderProduct);
}
