package kr.hhplus.be.server.api.order;

import kr.hhplus.be.server.api.order.dto.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    @PostMapping("/orders")
    public OrderResponseDto orders(@RequestBody OrderRequestDto orderRequestDto) {
        return new OrderResponseDto(
                10L,
                orderRequestDto.addressId(),
                "Youn Hanghae",
                "010-1234-5678",
                orderRequestDto.items(),
                129_000,
                "PAYMENT_PENDING"
        );
    }

    @PostMapping("/orders/payments")
    public PaymentResponseDto payments(@RequestBody PaymentRequestDto paymentRequestDto) {
        return new PaymentResponseDto(
                1L,
                paymentRequestDto.orderId(),
                "COMPLETED",
                paymentRequestDto.paymentMethod(),
                paymentRequestDto.amount()
        );
    }
}
