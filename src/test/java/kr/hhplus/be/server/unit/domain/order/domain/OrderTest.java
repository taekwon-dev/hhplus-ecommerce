package kr.hhplus.be.server.unit.domain.order.domain;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @DisplayName("Order 생성 - 성공")
    @Test
    void createOrder() {
        // given
        User user = UserFixture.USER();

        // when
        Order order = new Order(user);

        // then
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(order.getOrderProducts().size()).isZero();
    }

    @DisplayName("Order 에 OrderProduct 추가 - 성공")
    @Test
    void createAndAssociateOrderProduct() {
        // given
        User user = UserFixture.USER();
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);
        Order order = new Order(user);

        // when
        order.addOrderProduct(product, 1);

        // then
        assertThat(order.getOrderProducts().size()).isOne();
    }
}
