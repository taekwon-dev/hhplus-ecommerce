package kr.hhplus.be.server.unit.api.product.facade;

import kr.hhplus.be.server.api.product.controller.response.ProductResponse;
import kr.hhplus.be.server.api.product.facade.ProductFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(productService.findAllProducts()).thenReturn(List.of(product1, product2, product3));

        // when
        List<ProductResponse> responses = productFacade.findAllProducts();

        // then
        assertThat(responses).hasSize(3);

        verify(productService, times(1)).findAllProducts();
    }

    @DisplayName("가장 많이 팔린 상위 5개 Product 조회 - 성공")
    @Test
    void findTopSellingProducts() {
        // given
        Product product = ProductFixture.create(1L, 1_000, 10);

        User user = UserFixture.USER(1L);
        Order order = new Order(user);
        order.addOrderProduct(product, 1);

        when(productService.findTopSellingProducts()).thenReturn(List.of(product));

        // when
        List<ProductResponse> responses = productFacade.findTopSellingProducts();

        // then
        assertThat(responses).hasSize(1);

        verify(productService, times(1)).findTopSellingProducts();
    }
}
