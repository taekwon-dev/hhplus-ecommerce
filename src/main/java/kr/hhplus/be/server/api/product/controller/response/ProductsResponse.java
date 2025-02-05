package kr.hhplus.be.server.api.product.controller.response;

import kr.hhplus.be.server.api.product.application.dto.SellableProductsResult;

import java.util.List;
import java.util.stream.Collectors;

public record ProductsResponse(
        List<SellableProductsResult.ProductDetail> products
) {
    public static ProductsResponse from(SellableProductsResult result) {
        return new ProductsResponse(
                result.products().stream()
                        .map(product -> new SellableProductsResult.ProductDetail(
                                product.productId(),
                                product.categoryName(),
                                product.name(),
                                product.salesPrice(),
                                product.stockQuantity(),
                                product.createdAt()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
