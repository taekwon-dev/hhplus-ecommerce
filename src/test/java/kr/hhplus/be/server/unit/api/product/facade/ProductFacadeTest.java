package kr.hhplus.be.server.unit.api.product.facade;

import kr.hhplus.be.server.api.product.controller.response.BestSellingProductResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductAllResponse;
import kr.hhplus.be.server.api.product.facade.ProductFacade;
import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductFacade productFacade;

    @DisplayName("Product 모든 목록 조회 - 성공")
    @Test
    void findAllProducts() {
        // given
        Product product1 = ProductFixture.create(1L, 1_000, 10);
        Product product2 = ProductFixture.create(2L, 1_000, 10);
        Product product3 = ProductFixture.create(3L, 1_000, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(productService.findAllProducts(pageable)).thenReturn(new PageImpl<>(List.of(product1, product2, product3)));

        // when
        ProductAllResponse response = productFacade.findAllProducts(pageable);

        // then
        assertThat(response.products()).hasSize(3);

        verify(productService, times(1)).findAllProducts(pageable);
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

        when(productService.findBestSellingProducts(pageable)).thenReturn(bestSellingProducts);

        // when
        List<BestSellingProductResponse> result = productFacade.findBestSellingProducts(pageable);

        // then
        assertThat(result).hasSize(5);

        verify(productService, times(1)).findBestSellingProducts(pageable);
    }
}
