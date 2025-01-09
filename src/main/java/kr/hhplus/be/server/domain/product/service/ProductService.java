package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

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
            if (product.getStockQuantity() < productQuantityDto.quantity()) {
                throw new InsufficientStockException();
            }
            product.deductStockQuantity(productQuantityDto.quantity());
        }
    }
}
