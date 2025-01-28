package kr.hhplus.be.server.unit.domain.order.domain.product;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.product.OrderProduct;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    @DisplayName("주문 상품을 생성한다.")
    @Test
    void createOrderProduct() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        // when
        OrderProduct orderProduct = new OrderProduct(order, product, 1);

        // then
        assertThat(orderProduct.getOrder()).isEqualTo(order);
        assertThat(orderProduct.getProduct()).isEqualTo(product);
        assertThat(orderProduct.getQuantity()).isOne();
    }
}
