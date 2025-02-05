package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.infra.storage.core.ProductCoreRepository;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@DisplayName("상품 Service 단위 테스트")
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
        Product product = new Product(1L, name, category, price, stockQuantity);

        when(productCoreRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // when
        Product foundProduct = productService.findProductById(product.getId());

        // then
        assertThat(foundProduct.getName()).isEqualTo(product.getName());
        assertThat(foundProduct.getCategory()).isEqualTo(category);
        assertThat(foundProduct.getSalesPrice()).isEqualTo(price);
        assertThat(foundProduct.getStockQuantity()).isEqualTo(stockQuantity);

        verify(productCoreRepository, times(1)).findById(1L);
    }

    @DisplayName("요청한 수량만큼 상품 재고를 차감한다.")
    @Test
    void deductStock() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, price, stockQuantity);

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 5));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        when(productCoreRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

        // when & then
        assertThatCode(() -> productService.deductStock(param))
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
        Product product = new Product(1L, name, category, price, stockQuantity);

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 5));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        when(productCoreRepository.findByIdWithLock(product.getId())).thenReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.deductStock(param))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("판매 가능한 모든 상품 목록을 조회한다.")
    @Test
    void findSellableProducts() {
        // given
        SellableProduct product1 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product2 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product3 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);

        when(productCoreRepository.findSellableProducts(pageable)).thenReturn(List.of(product1, product2, product3));

        // when
        List<SellableProduct> products = productService.findSellableProducts(pageable);

        // then
        assertThat(products).hasSize(3);
        assertThat(products).contains(product1, product2, product3);

        verify(productCoreRepository, times(1)).findSellableProducts(pageable);
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
