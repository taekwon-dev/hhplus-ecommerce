package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
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
        Order order = new Order(1L, user.getId());

        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int salesPrice = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, salesPrice, stockQuantity);

        // when
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), salesPrice, 1);

        // then
        assertThat(orderProduct.getOrderId()).isEqualTo(order.getId());
        assertThat(orderProduct.getProductId()).isEqualTo(product.getId());
        assertThat(orderProduct.getSalesPrice()).isEqualTo(product.getSalesPrice());
        assertThat(orderProduct.getQuantity()).isOne();
    }
}
