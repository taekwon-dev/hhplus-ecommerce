package kr.hhplus.be.server.api.product.facade;

import kr.hhplus.be.server.api.product.controller.response.BestSellingProductResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductAllResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductResponse;
import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;

    @Transactional(readOnly = true)
    public ProductAllResponse findAllProducts(Pageable pageable) {
        Page<Product> products = productService.findAllProducts(pageable);
        return mapToProductResponses(products);
    }

    private ProductAllResponse mapToProductResponses(Page<Product> products) {
        List<ProductResponse> productResponses = products.stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity()))
                .toList();

        return new ProductAllResponse(productResponses, products.getNumber(), products.getTotalPages(), products.getSize());
    }

    @Transactional(readOnly = true)
    public List<BestSellingProductResponse> findBestSellingProducts(Pageable pageable) {
        List<BestSellingProduct> bestSellingProducts = productService.findBestSellingProducts(pageable);
        return mapToBestSellingProductResponses(bestSellingProducts);
    }

    private List<BestSellingProductResponse> mapToBestSellingProductResponses(List<BestSellingProduct> bestSellingProducts) {
        return bestSellingProducts.stream()
                .map(bestSellingProduct -> new BestSellingProductResponse(
                        bestSellingProduct.productId(),
                        bestSellingProduct.name(),
                        bestSellingProduct.price(),
                        bestSellingProduct.stockQuantity(),
                        bestSellingProduct.soldQuantity())
                ).toList();
    }
}
