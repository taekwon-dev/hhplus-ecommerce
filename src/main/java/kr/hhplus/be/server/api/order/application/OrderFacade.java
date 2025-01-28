package kr.hhplus.be.server.api.order.application;

import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final ProductService productService;
    private final OrderService orderService;

    @Transactional
    public Long order(Long userId, CreateOrderParam param) {
        productService.deductStock(param.toDeductStockParam());
        Order order = orderService.saveOrder(userId, param.toSaveOrderParam());
        return order.getId();
    }
}
