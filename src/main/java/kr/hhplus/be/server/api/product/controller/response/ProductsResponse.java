package kr.hhplus.be.server.api.product.controller.response;

import kr.hhplus.be.server.api.product.application.dto.ProductsResult;

import java.util.List;
import java.util.stream.Collectors;

public record ProductsResponse(
        List<ProductsResult.ProductDetail> products,
        int totalPages,
        int page,
        int size
) {
    public static ProductsResponse from(ProductsResult result) {
        return new ProductsResponse(
                result.products().stream()
                        .map(product -> new ProductsResult.ProductDetail(
                                product.productId(),
                                product.name(),
                                product.price(),
                                product.stockQuantity()
                        ))
                        .collect(Collectors.toList()),
                result.totalPages(),
                result.page(),
                result.size()
        );
    }
}
