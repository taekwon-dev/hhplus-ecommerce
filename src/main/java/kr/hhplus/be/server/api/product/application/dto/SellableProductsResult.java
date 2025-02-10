package kr.hhplus.be.server.api.product.application.dto;

import kr.hhplus.be.server.domain.product.model.SellableProduct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record SellableProductsResult(
        List<ProductDetail> products
) {
    public record ProductDetail(
            long productId,
            String categoryName,
            String name,
            int salesPrice,
            int stockQuantity,
            LocalDateTime createdAt
    ) {
    }

    public static SellableProductsResult from(List<SellableProduct> products) {
        return new SellableProductsResult(
                products.stream()
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
