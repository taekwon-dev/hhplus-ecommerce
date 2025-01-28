package kr.hhplus.be.server.api.product.application.dto;

import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public record ProductsResult(
        List<ProductDetail> products,
        int totalPages,
        int page,
        int size
) {
    public record ProductDetail(
            long productId,
            String name,
            int price,
            int stockQuantity
    ) {
    }

    public static ProductsResult from(Page<Product> products) {
        return new ProductsResult(
                products.stream()
                        .map(product -> new ProductsResult.ProductDetail(
                                product.getId(),
                                product.getName(),
                                product.getSalesPrice(),
                                product.getStockQuantity()
                        ))
                        .collect(Collectors.toList()),
                products.getTotalPages(),
                products.getNumber(),
                products.getSize()
        );
    }
}
