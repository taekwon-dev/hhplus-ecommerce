package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.PointJpaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointCoreRepositoryTest {

    @Mock
    private PointJpaRepository pointJpaRepository;

    @InjectMocks
    private PointCoreRepository pointCoreRepository;

    @DisplayName("포인트를 저장한다.")
    @Test
    void savePoint() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        Point point = new Point(user.getId(), balance);
        Point expectedPoint = new Point(1L, user.getId(), balance);

        when(pointJpaRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointCoreRepository.save(point);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUserId()).isEqualTo(expectedPoint.getUserId());
        assertThat(savedPoint.getBalance()).isEqualTo(expectedPoint.getBalance());

        verify(pointJpaRepository, times(1)).save(point);
    }

    @DisplayName("유저의 포인트 잔액을 조회한다.")
    @Test
    void findByUser() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        Point point = new Point(1L, user.getId(), balance);

        when(pointJpaRepository.findPointByUserId(user.getId())).thenReturn(Optional.of(point));

        // when
        Point foundPoint = pointCoreRepository.findByUserId(user.getId());

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUserId()).isEqualTo(point.getUserId());
        assertThat(foundPoint.getBalance()).isEqualTo(point.getBalance());

        verify(pointJpaRepository, times(1)).findPointByUserId(user.getId());
    }

    @DisplayName("유저의 포인트 잔액 조회 시, 포인트 잔액을 조회할 수 없는 경우 0으로 초기화 한 뒤 반환한다.")
    @Test
    void findByUser_initializeBalanceWithZero() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 0;
        Point point = new Point(1L, user.getId(), balance);

        when(pointJpaRepository.findPointByUserId(user.getId())).thenReturn(Optional.of(point));

        // when
        Point foundPoint = pointCoreRepository.findByUserId(user.getId());

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUserId()).isEqualTo(point.getUserId());
        assertThat(foundPoint.getBalance()).isZero();

        verify(pointJpaRepository, times(1)).findPointByUserId(user.getId());
    }
}
