package kr.hhplus.be.server.api.product.controller;

import kr.hhplus.be.server.api.product.controller.response.BestSellingProductResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductAllResponse;
import kr.hhplus.be.server.api.product.facade.ProductFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacade productFacade;

    @GetMapping
    public ResponseEntity<ProductAllResponse> findAllProducts(Pageable pageable) {
        ProductAllResponse response = productFacade.findAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<BestSellingProductResponse>> findBestSellingProducts(Pageable pageable) {
        List<BestSellingProductResponse> response = productFacade.findBestSellingProducts(pageable);
        return ResponseEntity.ok(response);
    }
}
