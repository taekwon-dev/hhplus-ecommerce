package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.exception.OrderNotFoundException;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.OrderJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCoreRepositoryTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @InjectMocks
    private OrderCoreRepository orderCoreRepository;

    @DisplayName("주문 정보를 저장한다.")
    @Test
    void saveOrder() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(user.getId());

        when(orderJpaRepository.save(order)).thenReturn(order);

        // when
        Order savedOrder = orderCoreRepository.save(order);

        // then
        assertThat(savedOrder.getUserId()).isEqualTo(order.getUserId());
        assertThat(savedOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(savedOrder.getOrderPrice()).isZero();

        verify(orderJpaRepository, times(1)).save(order);
    }

    @DisplayName("ID 기반으로 주문 정보를 조회한다.")
    @Test
    void findById() {
        // given
        User user = UserFixture.USER(1L);
        Order order = new Order(user.getId());

        when(orderJpaRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        Order foundOrder = orderCoreRepository.findById(1L);

        // then
        assertThat(foundOrder.getUserId()).isEqualTo(order.getUserId());
        assertThat(foundOrder.getStatus()).isEqualTo(order.getStatus());
        assertThat(foundOrder.getOrderPrice()).isZero();

        verify(orderJpaRepository, times(1)).findById(1L);
    }

    @DisplayName("ID 기반으로 주문 정보 조회 시, 주문 이력을 찾지 못한 경우 예외가 발생한다.")
    @Test
    void findById_doNotExist() {
        // given
        when(orderJpaRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderCoreRepository.findById(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
