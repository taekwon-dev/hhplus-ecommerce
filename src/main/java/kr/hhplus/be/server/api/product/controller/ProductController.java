package kr.hhplus.be.server.api.product.controller;

import kr.hhplus.be.server.api.product.application.dto.BestSellingProductsResult;
import kr.hhplus.be.server.api.product.application.dto.ProductsResult;
import kr.hhplus.be.server.api.product.controller.response.BestSellingProductsResponse;
import kr.hhplus.be.server.api.product.application.ProductFacade;
import kr.hhplus.be.server.api.product.controller.response.ProductsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacade productFacade;

    @GetMapping
    public ResponseEntity<ProductsResponse> findSellableProducts(Pageable pageable) {
        ProductsResult result = productFacade.findSellableProducts(pageable);
        return ResponseEntity.ok(ProductsResponse.from(result));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<BestSellingProductsResponse> findBestSellingProducts(Pageable pageable) {
        BestSellingProductsResult result = productFacade.findBestSellingProducts(pageable);
        return ResponseEntity.ok(BestSellingProductsResponse.from(result));
    }
}
