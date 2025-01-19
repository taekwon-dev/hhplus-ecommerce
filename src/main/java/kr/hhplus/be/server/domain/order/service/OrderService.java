package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.service.dto.OrderDetailDto;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order order(User user, List<OrderDetailDto> orderDetailDtos) {
        Order order = new Order(user);
        for (OrderDetailDto orderDetailDto : orderDetailDtos) {
            Product product = orderDetailDto.product();
            int quantity = orderDetailDto.quantity();
            order.addOrderProduct(product, quantity);
        }
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id);
    }

    public void validateOrderOwnership(User user, Order order) {
        order.validateOwnership(user);
    }

    public void completeOrder(Order order) {
        order.complete();
    }
}
