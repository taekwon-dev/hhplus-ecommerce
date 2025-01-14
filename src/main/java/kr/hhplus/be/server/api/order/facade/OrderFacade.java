package kr.hhplus.be.server.api.order.facade;

import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.service.dto.OrderDetailDto;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @Transactional
    public Long order(OrderCreateRequest request) {
        User user = userService.findUserById(request.userId());
        List<ProductQuantityDto> productQuantityDtos = mapToProductQuantityDtos(request);
        productService.validateStock(productQuantityDtos);
        List<OrderDetailDto> orderDetailDtos = mapToOrderDetailDtos(request);
        Order order = orderService.order(user, orderDetailDtos);
        return order.getId();
    }

    private List<ProductQuantityDto> mapToProductQuantityDtos(OrderCreateRequest request) {
        return request.orderProductDetails().stream()
                .map(orderProductDetail -> new ProductQuantityDto(orderProductDetail.productId(), orderProductDetail.quantity()))
                .collect(Collectors.toList());
    }

    private List<OrderDetailDto> mapToOrderDetailDtos(OrderCreateRequest request) {
        return request.orderProductDetails().stream()
                .map(orderProductDetail -> {
                    Product product = productService.findById(orderProductDetail.productId());
                    return new OrderDetailDto(product, orderProductDetail.quantity());
                })
                .collect(Collectors.toList());
    }
}
