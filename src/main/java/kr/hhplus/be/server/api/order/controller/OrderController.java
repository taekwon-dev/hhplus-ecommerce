package kr.hhplus.be.server.api.order.controller;

import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.order.controller.dto.request.CreateOrderRequest;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/v1/orders")
@RestController
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<Void> createOrder(User user, @RequestBody CreateOrderRequest request) {
        long orderId = orderFacade.order(user.getId(), request.toCreateOrderParam());
        return ResponseEntity.created(URI.create("/v1/orders/" + orderId)).build();
    }
}
