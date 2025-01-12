package kr.hhplus.be.server.api.product.facade;

import kr.hhplus.be.server.api.product.controller.response.ProductResponse;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllProducts() {
        List<Product> products = productService.findAllProducts();
        return mapToProductResponses(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findTopSellingProducts() {
        List<Product> products = productService.findTopSellingProducts();
        return mapToProductResponses(products);
    }

    private List<ProductResponse> mapToProductResponses(List<Product> products) {
        return products.stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity()))
                .collect(Collectors.toList());
    }
}
