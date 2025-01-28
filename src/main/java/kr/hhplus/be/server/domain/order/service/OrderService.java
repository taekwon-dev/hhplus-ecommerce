package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.order.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.service.dto.SaveOrderParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public Order saveOrder(Long userId, SaveOrderParam param) {
        Order order = orderRepository.save(new Order(userId));
        for (SaveOrderParam.Detail saveOrderParamDetail : param.saveOrderParamDetails()) {
            OrderProduct orderProduct = orderProductRepository.save(new OrderProduct(
                    order.getId(),
                    saveOrderParamDetail.productId(),
                    saveOrderParamDetail.salesPrice(),
                    saveOrderParamDetail.quantity()
            ));
            order.addOrderProduct(orderProduct);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id);
    }

    public void validateOrderOwnership(Long userId, Order order) {
        order.validateOwnership(userId);
    }

    public void completeOrder(Order order) {
        order.complete();
    }
}
