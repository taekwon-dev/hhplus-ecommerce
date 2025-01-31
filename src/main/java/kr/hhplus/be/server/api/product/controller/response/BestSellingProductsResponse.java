package kr.hhplus.be.server.api.product.controller.response;

import kr.hhplus.be.server.api.product.application.dto.BestSellingProductsResult;

import java.util.List;
import java.util.stream.Collectors;

public record BestSellingProductsResponse(
        List<BestSellingProductsResult.BestSellingProductDetail> products
) {
    public static BestSellingProductsResponse from(BestSellingProductsResult result) {
        return new BestSellingProductsResponse(
                result.products().stream()
                        .map(bestSellingProduct -> new BestSellingProductsResult.BestSellingProductDetail(
                                bestSellingProduct.productId(),
                                bestSellingProduct.name(),
                                bestSellingProduct.price(),
                                bestSellingProduct.stockQuantity(),
                                bestSellingProduct.soldQuantity()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
