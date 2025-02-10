package kr.hhplus.be.server.api.product.application;

import kr.hhplus.be.server.api.product.application.dto.BestSellingProductsResult;
import kr.hhplus.be.server.api.product.application.dto.SellableProductsResult;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;

    @Transactional(readOnly = true)
    public SellableProductsResult findSellableProducts(Pageable pageable) {
        List<SellableProduct> products = productService.findSellableProducts(pageable);
        return SellableProductsResult.from(products);
    }

    @Transactional(readOnly = true)
    public BestSellingProductsResult findBestSellingProducts(Pageable pageable) {
        List<BestSellingProduct> bestSellingProducts = productService.findBestSellingProducts(pageable);
        return BestSellingProductsResult.from(bestSellingProducts);
    }
}
