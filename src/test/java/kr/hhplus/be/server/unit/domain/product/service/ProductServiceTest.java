package kr.hhplus.be.server.unit.domain.product.service;

import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.repository.ProductCoreRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductCoreRepository productCoreRepository;

    @InjectMocks
    private ProductService productService;


    @DisplayName("Product ID 기반 조회 - 성공")
    @Test
    void findById() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        when(productCoreRepository.findById(1L)).thenReturn(product);

        // when
        Product foundProduct = productService.findById(1L);

        // then
        assertThat(foundProduct.getName()).isEqualTo(product.getName());
        assertThat(foundProduct.getCategory()).isEqualTo(category);
        assertThat(foundProduct.getPrice()).isEqualTo(price);
        assertThat(foundProduct.getStockQuantity()).isEqualTo(stockQuantity);

        verify(productCoreRepository, times(1)).findById(1L);
    }

    @DisplayName("Product 재고 여부 검증 - 성공 - 재고 있음")
    @Test
    void validateStock_withSufficientStock() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(1L, 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        when(productCoreRepository.findById(1L)).thenReturn(product);

        // when & then
        assertThatCode(() -> productService.validateStock(productQuantityDtos))
                .doesNotThrowAnyException();
    }

    @DisplayName("Product 재고 여부 검증 - 성공 - 재고 없음")
    @Test
    void validateStock_withInsufficientStock() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 1;
        Product product = new Product(name, category, price, stockQuantity);

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(1L, 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        when(productCoreRepository.findById(1L)).thenReturn(product);

        // when & then
        assertThatThrownBy(() -> productService.validateStock(productQuantityDtos))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("Product 재고 차감 - 성공")
    @Test
    void deductStock() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(1L, 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        when(productCoreRepository.findByIdWithLock(1L)).thenReturn(product);

        // when & then
        assertThatCode(() -> productService.deductStock(productQuantityDtos))
                .doesNotThrowAnyException();
    }

    @DisplayName("Product 재고 차감 - 실패 - 재고 부족")
    @Test
    void deductStock_Fail_InvalidStockQuantity() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 1;
        Product product = new Product(name, category, price, stockQuantity);

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(1L, 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        when(productCoreRepository.findByIdWithLock(1L)).thenReturn(product);

        // when & then
        assertThatThrownBy(() -> productService.deductStock(productQuantityDtos))
                .isInstanceOf(InsufficientStockException.class);
    }
}
