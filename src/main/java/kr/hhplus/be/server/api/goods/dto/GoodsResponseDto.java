package kr.hhplus.be.server.api.goods.dto;

import java.util.List;

public record GoodsResponseDto(
        Long id,
        String name,
        int price,
        int quantity,
        List<String> colors,
        List<String> sizes
) {}
