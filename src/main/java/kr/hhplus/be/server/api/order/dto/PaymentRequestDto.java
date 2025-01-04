package kr.hhplus.be.server.api.order.dto;

public record PaymentRequestDto(
        Long orderId,
        String paymentMethod,
        int amount
) {}
