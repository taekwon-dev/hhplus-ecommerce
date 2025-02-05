package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    @Transactional
    public void deductStock(DeductStockParam param) {
        for (DeductStockParam.Detail deductStockDetail : param.deductStockParamDetails()) {
            deductStock(deductStockDetail.productId(), deductStockDetail.quantity());
        }
    }

    @Transactional
    public void deductStock(long productId, int quantity) {
        Product product = productRepository.findByIdWithLock(productId).orElseThrow(ProductNotFoundException::new);
        product.deductStock(quantity);
    }

    @Transactional(readOnly = true)
    public List<Product> findSellableProducts(Pageable pageable) {
        return productRepository.findSellableProducts(pageable);
    }

    @Transactional(readOnly = true)
    public List<BestSellingProduct> findBestSellingProducts(Pageable pageable) {
        LocalDateTime endDateTime = LocalDateTime.now(clock);
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return productRepository.findBestSellingProducts(startDateTime, endDateTime, pageable);
    }
}
