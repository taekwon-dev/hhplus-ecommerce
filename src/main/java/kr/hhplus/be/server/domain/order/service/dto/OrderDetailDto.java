package kr.hhplus.be.server.domain.order.service.dto;

import kr.hhplus.be.server.domain.product.domain.Product;

public record OrderDetailDto(
        Product product,
        int quantity
) {
}
