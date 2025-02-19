package kr.hhplus.be.server.domain.support.cache.aop;

import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.domain.support.cache.ProductCacheManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class ProductCacheAspect {

    private final ProductRepository productRepository;
    private final ProductCacheManager productCacheManager;

    @Around("execution(* kr.hhplus.be.server.domain.product.service.ProductService.findSellableProducts(..))")
    public Object fetchOrCacheSellableProducts(ProceedingJoinPoint joinPoint) throws Throwable {
        Pageable pageable = (Pageable) joinPoint.getArgs()[0];
        Object cachedProducts = productCacheManager.fetchSellableProducts(pageable);
        if (!((List<?>) cachedProducts).isEmpty()) {
            return cachedProducts;
        }

        Object result = joinPoint.proceed();
        List<SellableProduct> products = ((List<?>) result).stream()
                .filter(SellableProduct.class::isInstance)
                .map(SellableProduct.class::cast)
                .toList();

        productCacheManager.cacheSellableProducts(products);
        return result;
    }

    @Around("execution(* kr.hhplus.be.server.domain.product.service.ProductService.deductStock(..))")
    public void evictOutOfStockProductsFromCache(ProceedingJoinPoint joinPoint) throws Throwable {
        joinPoint.proceed();

        DeductStockParam param = (DeductStockParam) joinPoint.getArgs()[0];
        for (DeductStockParam.Detail detail : param.deductStockParamDetails()) {
            long productId = detail.productId();
            productRepository.findById(productId).ifPresent(productCacheManager::evictOutOfStockProduct);
        }
    }
}
