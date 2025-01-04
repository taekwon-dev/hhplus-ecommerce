package kr.hhplus.be.server.api.order.dto;

public record OrderItemDto(
        Long goodsId,
        int quantity,
        String size,
        String color
) {}