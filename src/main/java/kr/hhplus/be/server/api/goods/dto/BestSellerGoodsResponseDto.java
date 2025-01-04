package kr.hhplus.be.server.api.goods.dto;

public record BestSellerGoodsResponseDto(
        Long id,
        String name,
        int soldCount,
        int price,
        String color,
        String size
) {}

