package kr.hhplus.be.server.domain.support.cache;

import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("상품 캐시 매니저 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProductCacheManagerTest {

    private static final String PRODUCT_SORTED_SET_KEY = "products:created_at:desc";
    private static final String PRODUCT_HASH_PREFIX = "products:";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ProductCacheManager productCacheManager;

    @DisplayName("판매 가능한 상품 목록 저장된 캐시에서 데이터를 조회한다.")
    @Test
    void fetchSellableProducts() {
        // given
        long productId = 1L;
        Pageable pageable = PageRequest.of(0, 1);
        int from = pageable.getPageNumber() * pageable.getPageSize();
        int to = from + pageable.getPageSize() - 1;
        Map<Object, Object> productMap = Map.of(
                "categoryName", "상의",
                "name", "라넌큘러스 오버핏 맨투맨",
                "salePrice", 10_000,
                "stockQuantity", 10,
                "createdAt", LocalDateTime.now()
        );

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(zSetOperations.range(PRODUCT_SORTED_SET_KEY, from, to)).thenReturn(Set.of(String.valueOf(productId)));
        when(hashOperations.entries(PRODUCT_HASH_PREFIX + productId)).thenReturn(productMap);

        // when
        List<SellableProduct> products = productCacheManager.fetchSellableProducts(pageable);

        // then
        verify(zSetOperations, times(1)).range(PRODUCT_SORTED_SET_KEY, from, to);
        verify(hashOperations, times(1)).entries(PRODUCT_HASH_PREFIX + productId);

        assertThat(products).hasSize(1);
    }


    @DisplayName("판매 가능한 상품 목록을 캐시에 저장한다.")
    @Test
    void cacheSellableProducts() {
        // given
        SellableProduct product = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // when
        productCacheManager.cacheSellableProducts(List.of(product));

        // then
        verify(zSetOperations, times(1)).add(PRODUCT_SORTED_SET_KEY, String.valueOf(product.productId()), product.createdAt().toEpochSecond(ZoneOffset.UTC));
        verify(hashOperations, times(1)).putAll(PRODUCT_HASH_PREFIX + product.productId(), Map.of(
                        "name", product.name(),
                        "categoryName", product.categoryName(),
                        "salePrice", product.salesPrice(),
                        "stockQuantity", product.stockQuantity(),
                        "createdAt", product.createdAt()
                )
        );
    }

    @DisplayName("재고가 0인 상품을 캐시에서 제거한다.")
    @Test
    void evictOutOfStockProduct() {
        // given
        long productId = 1L;
        Product product = mock(Product.class);

        when(product.getId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(0);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // when
        productCacheManager.evictOutOfStockProduct(product);

        // then
        verify(zSetOperations, times(1)).remove(PRODUCT_SORTED_SET_KEY, String.valueOf(productId));
        verify(redisTemplate, times(1)).delete(PRODUCT_HASH_PREFIX + productId);
    }

    @DisplayName("재고가 1 이상인 상품은 캐시에서 제거하지 않는다.")
    @Test
    void evictOutOfStockProduct_whenStockIsPositive() {
        // given
        long productId = 1L;
        Product product = mock(Product.class);

        when(product.getStockQuantity()).thenReturn(10);

        // when
        productCacheManager.evictOutOfStockProduct(product);

        // then
        verify(zSetOperations, never()).remove(PRODUCT_SORTED_SET_KEY, productId);
        verify(redisTemplate, never()).delete(PRODUCT_HASH_PREFIX + productId);
    }
}