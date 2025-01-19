package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
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

    @Transactional(readOnly = true)
    public void validateStock(List<ProductQuantityDto> productQuantityDtos) {
        for (ProductQuantityDto productQuantityDto : productQuantityDtos) {
            Product product = productRepository.findById(productQuantityDto.productId());
            if (product.getStockQuantity() < productQuantityDto.quantity()) {
                throw new InsufficientStockException();
            }
        }
    }

    @Transactional
    public void deductStock(List<ProductQuantityDto> productQuantityDtos) {
        for (ProductQuantityDto productQuantityDto : productQuantityDtos) {
            Product product = productRepository.findByIdWithLock(productQuantityDto.productId());
            product.deductStockQuantity(productQuantityDto.quantity());
        }
    }

    @Transactional(readOnly = true)
    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAllProducts(pageable);
    }

    @Transactional(readOnly = true)
    public List<BestSellingProduct> findBestSellingProducts(Pageable pageable) {
        LocalDateTime endDateTime = LocalDateTime.now(clock);
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return productRepository.findBestSellingProducts(startDateTime, endDateTime, pageable);
    }
}
