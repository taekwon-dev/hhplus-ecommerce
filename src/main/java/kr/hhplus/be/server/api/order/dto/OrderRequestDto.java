package kr.hhplus.be.server.api.order.dto;

import java.util.List;

public record OrderRequestDto(
        Long addressId,
        List<OrderItemDto> items,
        Long totalPrice
) {
}
