package kr.hhplus.be.server.unit.domain.order.domain;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

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

    @DisplayName("Order 에 포함된 상품 총 결제 금액 계산 - 성공")
    @Test
    void calculateTotalPrice() {
        // given
        User user = UserFixture.USER();
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);
        Order order = new Order(user);
        order.addOrderProduct(product, 1);

        // when
        int totalPrice = order.calculateTotalPrice();

        // then
        assertThat(totalPrice).isEqualTo(price);
    }

    @DisplayName("Order 결제 완료 상태 변경 - 성공")
    @Test
    void complete() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        // when
        order.complete();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }

    @DisplayName("Order 소유자 검증 - 성공")
    @Test
    void validateOwnership() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        // when & then
        assertThatCode(() -> order.validateOwnership(user))
                .doesNotThrowAnyException();
    }

    @DisplayName("Order 소유자 검증 - 실패 - 유저가 생성한 주문이 아닌 경우 예외 발생")
    @Test
    void validateOrderOwnership_Fail_NotMine() {
        // given
        User user1 = UserFixture.USER(1L);
        User user2 = UserFixture.USER(2L);

        Order order = new Order(user1);

        // when & then
        assertThatThrownBy(() -> order.validateOwnership(user2))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
