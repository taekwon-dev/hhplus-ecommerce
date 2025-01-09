package kr.hhplus.be.server.unit.domain.order.repository;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.repository.OrderCoreRepository;
import kr.hhplus.be.server.domain.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCoreRepositoryTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @InjectMocks
    private OrderCoreRepository orderCoreRepository;

    @DisplayName("Order 저장 - 성공")
    @Test
    void saveOrder() {
        // given
        User user = UserFixture.USER();
        Order order = new Order(user);

        when(orderJpaRepository.save(order)).thenReturn(order);

        // when
        Order savedOrder = orderCoreRepository.save(order);

        // then
        assertThat(savedOrder.getUser()).isEqualTo(order.getUser());
        assertThat(savedOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(savedOrder.getOrderProducts().size()).isZero();

        verify(orderJpaRepository, times(1)).save(order);
    }
}
