package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    public boolean validateStock(List<ProductQuantityDto> productQuantityDtos) {
        for (ProductQuantityDto productQuantityDto : productQuantityDtos) {
            Product product = productRepository.findById(productQuantityDto.productId());
            if (product.getStockQuantity() < productQuantityDto.quantity()) {
                return false;
            }
        }
        return true;
    }
}
