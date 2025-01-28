package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @DisplayName("상품을 생성한다.")
    @Test
    void createProduct() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 100;

        // when
        Product product = new Product(name, category, salesPrice, stockQuantity);

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSalesPrice()).isEqualTo(salesPrice);
        assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
    }

    @DisplayName("요청한 수량만큼 상품 재고량를 차감한다.")
    @Test
    void deductStock() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 10;
        Product product = new Product(name, category, salesPrice, stockQuantity);

        // when
        product.deductStock(10);

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSalesPrice()).isEqualTo(salesPrice);
        assertThat(product.getStockQuantity()).isZero();
    }

    @DisplayName("상품 재고량 차감 시, 요청한 수량보다 상품의 재고량이 부족한 경우 예외가 발생한다.")
    @Test
    void deductStock_InsufficientStockQuantity() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, salesPrice, stockQuantity);

        // when & then
        assertThatThrownBy(() -> product.deductStock(stockQuantity + 1))
                .isInstanceOf(InsufficientStockException.class);
    }
}
