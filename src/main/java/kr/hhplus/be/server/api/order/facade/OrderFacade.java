package kr.hhplus.be.server.api.order.facade;

import kr.hhplus.be.server.api.order.controller.request.OrderRequest;
import kr.hhplus.be.server.api.order.controller.response.OrderResponse;
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
    public OrderResponse order(long userId, List<OrderRequest> requests) {
        User user = userService.findUserById(userId);
        List<ProductQuantityDto> productQuantityDtos = mapToProductQuantityDtos(requests);
        productService.validateStock(productQuantityDtos);

        List<OrderDetailDto> orderDetailDtos = mapToOrderDetailDtos(requests);
        Order order = orderService.order(user, orderDetailDtos);

        return new OrderResponse(order.getId(), order.getStatus().name());
    }

    private List<ProductQuantityDto> mapToProductQuantityDtos(List<OrderRequest> requests) {
        return requests.stream()
                .map(request -> new ProductQuantityDto(request.productId(), request.quantity()))
                .collect(Collectors.toList());
    }

    private List<OrderDetailDto> mapToOrderDetailDtos(List<OrderRequest> requests) {
        return requests.stream()
                .map(request -> {
                    Product product = productService.findById(request.productId());
                    return new OrderDetailDto(product, request.quantity());
                })
                .collect(Collectors.toList());
    }
}
