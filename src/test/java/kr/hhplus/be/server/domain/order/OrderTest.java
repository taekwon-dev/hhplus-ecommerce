package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class OrderTest {

    @DisplayName("주문 정보을 생성한다.")
    @Test
    void createOrder() {
        // given
        User user = UserFixture.USER(1L);

        // when
        Order order = new Order(user.getId());

        // then
        assertThat(order.getUserId()).isEqualTo(user.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @DisplayName("주문의 총 결제 금액에 각 상품의 결제 금액을 추가한다.")
    @Test
    void createAndAssociateOrderProduct() {
        // given
        User user = UserFixture.USER(1L);
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(1L, name, category, price, stockQuantity);
        Order order = new Order(1L, user.getId());
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), product.getSalesPrice(),1);

        // when
        order.addOrderProduct(orderProduct);

        // then
        assertThat(order.getOrderPrice()).isEqualTo(product.getSalesPrice());
    }

    @DisplayName("주문 상태를 결제 완료 상태로 변경한다.")
    @Test
    void complete() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(user.getId());

        // when
        order.completePayment();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }

    @DisplayName("주문자를 검증한다.")
    @Test
    void validateOwnership() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(user.getId());

        // when & then
        assertThatCode(() -> order.validateOwnership(user.getId()))
                .doesNotThrowAnyException();
    }

    @DisplayName("주문자 검증 시, 해당 주문을 요청한 유저가 아닌 경우 예외가 발생한다.")
    @Test
    void validateOrderOwnership_notMine() {
        // given
        User user1 = UserFixture.USER(1L);
        User user2 = UserFixture.USER(2L);

        Order order = new Order(user1.getId());

        // when & then
        assertThatThrownBy(() -> order.validateOwnership(user2.getId()))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
