package kr.hhplus.be.server.unit.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.ProductCoreRepository;
import kr.hhplus.be.server.domain.product.repository.ProductJpaRepository;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCoreRepositoryTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @InjectMocks
    private ProductCoreRepository productCoreRepository;

    @DisplayName("Product 저장 - 성공")
    @Test
    void saveProduct() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        when(productJpaRepository.save(product)).thenReturn(product);

        // when
        Product savedProduct = productCoreRepository.save(product);

        // then
        assertThat(savedProduct.getName()).isEqualTo(product.getName());
        assertThat(savedProduct.getCategory()).isEqualTo(product.getCategory());
        assertThat(savedProduct.getPrice()).isEqualTo(product.getPrice());
        assertThat(savedProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());

        verify(productJpaRepository, times(1)).save(product);
    }
}
