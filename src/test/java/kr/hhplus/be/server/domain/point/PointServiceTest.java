package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.infra.storage.core.PointCoreRepository;
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
class PointServiceTest {

    @Mock
    private PointCoreRepository pointCoreRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("유저의 포인트 잔액을 조회한다.")
    @Test
    void findPointByUser() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        Point point = new Point(1L, user.getId(), balance);

        when(pointCoreRepository.findByUserId(user.getId())).thenReturn(point);

        // when
        Point foundPoint = pointService.findPointByUserId(user.getId());

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUserId()).isEqualTo(point.getUserId());
        assertThat(foundPoint.getBalance()).isEqualTo(point.getBalance());

        verify(pointCoreRepository, times(1)).findByUserId(user.getId());
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void charge() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 0;
        int amount = 1_000;
        Point point = new Point(1L, user.getId(), balance);
        Point expectedPoint = new Point(1L, user.getId(), balance + amount);

        when(pointCoreRepository.findByUserIdWithLock(user.getId())).thenReturn(point);
        when(pointCoreRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointService.charge(user.getId(), amount);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUserId()).isEqualTo(expectedPoint.getUserId());
        assertThat(savedPoint.getBalance()).isEqualTo(expectedPoint.getBalance());

        verify(pointCoreRepository, times(1)).findByUserIdWithLock(user.getId());
        verify(pointCoreRepository, times(1)).save(expectedPoint);
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void deduct() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        int amount = 1_000;
        Point point = new Point(1L, user.getId(), balance);
        Point expectedPoint = new Point(1L, user.getId(), balance - amount);

        when(pointCoreRepository.findByUserIdWithLock(user.getId())).thenReturn(point);
        when(pointCoreRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointService.deduct(user.getId(), amount);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUserId()).isEqualTo(expectedPoint.getUserId());
        assertThat(savedPoint.getBalance()).isZero();

        verify(pointCoreRepository, times(1)).findByUserIdWithLock(user.getId());
        verify(pointCoreRepository, times(1)).save(expectedPoint);
    }
}
