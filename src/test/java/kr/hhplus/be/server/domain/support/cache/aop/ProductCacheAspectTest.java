package kr.hhplus.be.server.domain.support.cache.aop;

import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.domain.support.cache.ProductCacheManager;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@DisplayName("상품 캐시 Aspect 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProductCacheAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private ProductCacheManager productCacheManager;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCacheAspect aspect;

    @DisplayName("판매 가능한 상품 목록 조회 시, 캐시 히트인 경우 데이터베이스 조회를 하지 않는다.")
    @Test
    void fetchOrCacheSellableProducts_cacheHit() throws Throwable {
        // given
        SellableProduct product1 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product2 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product3 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 20);

        when(productCacheManager.fetchSellableProducts(pageable)).thenReturn(List.of(product1, product2, product3));
        when(joinPoint.getArgs()).thenReturn(new Object[]{pageable});

        // when
        aspect.fetchOrCacheSellableProducts(joinPoint);

        // then
        verify(productCacheManager, times(1)).fetchSellableProducts(pageable);
        verify(joinPoint, never()).proceed();
        verify(productCacheManager, never()).cacheSellableProducts(List.of(product1, product2, product3));
    }

    @DisplayName("판매 가능한 상품 목록 조회 시, 캐시 미스인 경우 데이터베이스에서 읽어온 결과를 캐시에 업데이트한다.")
    @Test
    void fetchOrCacheSellableProducts_cacheMiss() throws Throwable {
        // given
        SellableProduct product1 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product2 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product3 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 20);

        when(productCacheManager.fetchSellableProducts(pageable)).thenReturn(Collections.emptyList());
        when(joinPoint.getArgs()).thenReturn(new Object[]{pageable});
        when(joinPoint.proceed()).thenReturn(List.of(product1, product2, product3));

        // when
        aspect.fetchOrCacheSellableProducts(joinPoint);

        // then
        verify(productCacheManager, times(1)).fetchSellableProducts(pageable);
        verify(joinPoint, times(1)).proceed();
        verify(productCacheManager, times(1)).cacheSellableProducts(List.of(product1, product2, product3));
    }

    @DisplayName("재고 차감 시, 재고가 0인 상품은 캐시에서 제거된다.")
    @Test
    void evictOutOfStockProductsFromCache() throws Throwable {
        // given
        Product product1 = new Product(1L, "라넌큘러스 오버핏 맨투맨", CategoryFixture.create("상의"), 1_000, 1);
        Product product2 = new Product(2L, "라넌큘러스 오버핏 맨투맨", CategoryFixture.create("상의"), 1_000, 1);
        DeductStockParam.Detail detail1 = new DeductStockParam.Detail(product1.getId(), 1);
        DeductStockParam.Detail detail2 = new DeductStockParam.Detail(product2.getId(), 1);
        List<DeductStockParam.Detail> deductStockParamDetails = List.of(detail1, detail2);
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        when(joinPoint.getArgs()).thenReturn(new Object[]{param});
        when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2.getId())).thenReturn(Optional.of(product2));

        // when
        aspect.evictOutOfStockProductsFromCache(joinPoint);

        // then
        verify(productRepository, times(1)).findById(product1.getId());
        verify(productRepository, times(1)).findById(product2.getId());
        verify(productCacheManager, times(1)).evictOutOfStockProduct(product1);
        verify(productCacheManager, times(1)).evictOutOfStockProduct(product2);
    }
}
