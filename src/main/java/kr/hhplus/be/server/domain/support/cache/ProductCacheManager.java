package kr.hhplus.be.server.domain.support.cache;

import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductCacheManager {

    private static final String PRODUCT_SORTED_SET_KEY = "products:created_at:desc";
    private static final String PRODUCT_HASH_PREFIX = "products:";

    private static final String FIELD_CATEGORY_NAME = "categoryName";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SALE_PRICE = "salePrice";
    private static final String FIELD_STOCK_QUANTITY = "stockQuantity";
    private static final String FIELD_CREATED_AT = "createdAt";

    private final RedisTemplate<String, Object> redisTemplate;

    public List<SellableProduct> fetchSellableProducts(Pageable pageable) {
        int from = pageable.getPageNumber() * pageable.getPageSize();
        int to = from + pageable.getPageSize() - 1;
        Set<Object> productIds = redisTemplate.opsForZSet().range(PRODUCT_SORTED_SET_KEY, from, to);

        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productIds.stream()
                .map(id -> retrieveProduct(Long.parseLong(id.toString())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<SellableProduct> retrieveProduct(Long productId) {
        Map<Object, Object> product = redisTemplate.opsForHash().entries(PRODUCT_HASH_PREFIX + productId);
        if (product.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SellableProduct(
                productId,
                (String) product.get(FIELD_CATEGORY_NAME),
                (String) product.get(FIELD_NAME),
                (Integer) product.get(FIELD_SALE_PRICE),
                (Integer) product.get(FIELD_STOCK_QUANTITY),
                (LocalDateTime) product.get(FIELD_CREATED_AT))
        );
    }

    public void cacheSellableProducts(List<SellableProduct> products) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        for (SellableProduct product : products) {
            zSetOps.add(PRODUCT_SORTED_SET_KEY, String.valueOf(product.productId()), product.createdAt().toEpochSecond(ZoneOffset.UTC));
            redisTemplate.opsForHash().putAll(PRODUCT_HASH_PREFIX + product.productId(),
                    Map.of(
                        FIELD_NAME, product.name(),
                        FIELD_CATEGORY_NAME, product.categoryName(),
                        FIELD_SALE_PRICE, product.salesPrice(),
                        FIELD_STOCK_QUANTITY, product.stockQuantity(),
                        FIELD_CREATED_AT, product.createdAt()
                    )
            );
        }
    }

    public void evictOutOfStockProduct(Product product) {
        if (product.getStockQuantity() > 0) {
            return;
        }
        redisTemplate.opsForZSet().remove(PRODUCT_SORTED_SET_KEY, String.valueOf(product.getId()));
        redisTemplate.delete(PRODUCT_HASH_PREFIX + product.getId());
    }
}
