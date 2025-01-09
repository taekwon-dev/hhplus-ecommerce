package kr.hhplus.be.server.unit.domain.product.domain;

import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @DisplayName("Product 생성 - 성공")
    @Test
    void createProduct() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;

        // when
        Product product = new Product(name, category, price, stockQuantity);

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
    }

    @DisplayName("Product 재고 차감 - 성공")
    @Test
    void deductStockQuantity() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        // when
        product.deductStockQuantity(stockQuantity);

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStockQuantity()).isZero();
    }

    @DisplayName("Product 재고 차감 - 실패 - 재고 부족")
    @Test
    void deductStockQuantity_Fail_InvalidStockQuantity() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        // when & then
        assertThatThrownBy(() -> product.deductStockQuantity(stockQuantity + 100))
                .isInstanceOf(InsufficientStockException.class);
    }
}
