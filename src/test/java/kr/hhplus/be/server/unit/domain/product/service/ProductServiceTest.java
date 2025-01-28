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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductCoreRepository productCoreRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ProductService productService;

    @DisplayName("ID 기반으로 상품을 조회한다.")
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

    @DisplayName("주문 수량 만큼 상품 재고가 충분한 경우 검증에 성공한다.")
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

    @DisplayName("주문 수량 만큼 상품 재고가 충분하지 않은 경우 예외가 발생한다.")
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

    @DisplayName("요청한 수량만큼 상품 재고를 차감한다.")
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

    @DisplayName("상품 재고 차감 시, 요청한 수량보다 상품의 재고가 부족한 경우 예외가 발생한다.")
    @Test
    void deductStock_InsufficientStock() {
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

    @DisplayName("판매 가능한 모든 상품 목록을 조회한다.")
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

    @DisplayName("지난 3일 동안 가장 많이 팔린 상위 5개 상품 목록을 조회한다.")
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

        when(clock.instant()).thenReturn(Instant.parse("2025-01-15T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(productCoreRepository.findBestSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable))).thenReturn(bestSellingProducts);

        // when
        List<BestSellingProduct> result = productService.findBestSellingProducts(pageable);

        // then
        assertThat(result).hasSize(5);

        verify(productCoreRepository, times(1)).findBestSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
    }
}
