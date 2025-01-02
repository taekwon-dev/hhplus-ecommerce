package kr.hhplus.be.server.api.order;

import kr.hhplus.be.server.api.order.dto.OrderItemDto;
import kr.hhplus.be.server.api.order.dto.OrderRequestDto;
import kr.hhplus.be.server.api.order.dto.OrderResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @PostMapping("/orders")
    public OrderResponseDto orders(@RequestBody OrderRequestDto orderRequestDto) {
        return new OrderResponseDto(
                10L,
                101L,
                "Youn Hanghae",
                "010-1234-5678",
                List.of(new OrderItemDto(2L, 1, "M", "Black")),
                129_000,
                "PAYMENT_PENDING"
        );
    }
}
