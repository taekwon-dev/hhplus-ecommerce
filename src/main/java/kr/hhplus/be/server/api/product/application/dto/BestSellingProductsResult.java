package kr.hhplus.be.server.api.product.application.dto;

import kr.hhplus.be.server.domain.product.model.BestSellingProduct;

import java.util.List;
import java.util.stream.Collectors;

public record BestSellingProductsResult(
        List<BestSellingProductDetail> products
) {
    public record BestSellingProductDetail(
            long productId,
            String name,
            int salesPrice,
            int stockQuantity,
            long soldQuantity
    ) {
    }

    public static BestSellingProductsResult from(List<BestSellingProduct> bestSellingProducts) {
        return new BestSellingProductsResult(
                bestSellingProducts.stream()
                        .map(bestSellingProduct -> new BestSellingProductsResult.BestSellingProductDetail(
                                bestSellingProduct.productId(),
                                bestSellingProduct.name(),
                                bestSellingProduct.salesPrice(),
                                bestSellingProduct.stockQuantity(),
                                bestSellingProduct.soldQuantity()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
