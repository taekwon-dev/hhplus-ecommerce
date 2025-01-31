package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void deductStock(DeductStockParam param) {
        for (DeductStockParam.Detail deductStockDetail : param.deductStockParamDetails()) {
            Product product = productRepository.findByIdWithLock(deductStockDetail.productId());
            product.deductStock(deductStockDetail.quantity());
        }
    }

    @Transactional(readOnly = true)
    public Page<Product> findSellableProducts(Pageable pageable) {
        return productRepository.findSellableProducts(pageable);
    }

    @Transactional(readOnly = true)
    public List<BestSellingProduct> findBestSellingProducts(Pageable pageable) {
        LocalDateTime endDateTime = LocalDateTime.now(clock);
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return productRepository.findBestSellingProducts(startDateTime, endDateTime, pageable);
    }
}
