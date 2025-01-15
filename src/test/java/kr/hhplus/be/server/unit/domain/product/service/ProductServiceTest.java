package kr.hhplus.be.server.unit.domain.product.service;

import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.repository.ProductCoreRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @DisplayName("Product 모든 목록 조회 - 성공")
    @Test
    void findAllProducts() {
        // given
        Product product1 = ProductFixture.create(1L, 1_000, 10);
        Product product2 = ProductFixture.create(2L, 1_000, 10);
        Product product3 = ProductFixture.create(3L, 1_000, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(productCoreRepository.findAllProducts(pageable)).thenReturn(new PageImpl<>(List.of(product1, product2, product3)));

        // when
        Page<Product> products = productService.findAllProducts(pageable);

        // then
        assertThat(products).hasSize(3);
        assertThat(products).contains(product1, product2, product3);

        verify(productCoreRepository, times(1)).findAllProducts(pageable);
    }

    @DisplayName("가장 많이 팔린 상위 5개 Product 조회 - 성공")
    @Test
    void findBestSellingProducts() {
        // given
        List<BestSellingProduct> bestSellingProducts = List.of(
                new BestSellingProduct(1L, "라넌큘러스 오버핏 맨투맨1", 10_000, 100, 50),
                new BestSellingProduct(1L, "라넌큘러스 오버핏 맨투맨2", 10_000, 100, 60),
                new BestSellingProduct(1L, "라넌큘러스 오버핏 맨투맨3", 10_000, 100, 70),
                new BestSellingProduct(1L, "라넌큘러스 오버핏 맨투맨4", 10_000, 100, 80),
                new BestSellingProduct(1L, "라넌큘러스 오버핏 맨투맨5", 10_000, 100, 90)
        );
        Pageable pageable = PageRequest.ofSize(5);

        when(productCoreRepository.findBestSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable))).thenReturn(bestSellingProducts);

        // when
        List<BestSellingProduct> result = productService.findBestSellingProducts(pageable);

        // then
        assertThat(result).hasSize(5);

        verify(productCoreRepository, times(1)).findBestSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
    }
}
