package kr.hhplus.be.server.api.order.controller;

import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
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
    public ResponseEntity<Void> createOrder(
            @RequestBody OrderCreateRequest request,
            User user
    ) {
        long orderId = orderFacade.order(user, request);
        return ResponseEntity.created(URI.create("/v1/orders/" + orderId)).build();
    }
}
