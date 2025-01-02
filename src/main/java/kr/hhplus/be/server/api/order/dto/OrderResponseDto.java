package kr.hhplus.be.server.api.order.dto;

import java.util.List;

public record OrderResponseDto(
        Long orderId,
        Long addressId,
        String recipientName,
        String phoneNumber,
        List<OrderItemDto> orderItems,
        int totalPrice,
        String status
) {
}