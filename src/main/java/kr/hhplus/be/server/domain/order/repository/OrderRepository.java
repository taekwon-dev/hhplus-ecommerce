package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.domain.Order;

public interface OrderRepository {

    Order save(Order order);
}
