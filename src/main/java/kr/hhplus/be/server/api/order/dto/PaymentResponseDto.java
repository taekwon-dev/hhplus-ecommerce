package kr.hhplus.be.server.api.order.dto;

public record PaymentResponseDto(
        Long paymentId,
        Long orderId,
        String status,
        String paymentMethod,
        int amount
) {}