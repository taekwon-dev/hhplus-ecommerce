package kr.hhplus.be.server.unit.domain.point.service;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointCoreRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
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

    @DisplayName("Point User 기반 조회 - 성공")
    @Test
    void findPointByUser() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        Point point = new Point(1L, user, balance);

        when(pointCoreRepository.findByUser(user)).thenReturn(point);

        // when
        Point foundPoint = pointService.findPointByUser(user);

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUser()).isEqualTo(point.getUser());
        assertThat(foundPoint.getBalance()).isEqualTo(point.getBalance());

        verify(pointCoreRepository, times(1)).findByUser(user);
    }

    @DisplayName("Point 추가 - 성공")
    @Test
    void addPoints() {
        // given
        User user = UserFixture.USER();
        int balance = 0;
        int amount = 1_000;
        Point point = new Point(1L, user, balance);
        Point expectedPoint = new Point(1L, user, balance + amount);

        when(pointCoreRepository.findByUserWithLock(user)).thenReturn(point);
        when(pointCoreRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointService.addPoints(user, amount);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUser()).isEqualTo(expectedPoint.getUser());
        assertThat(savedPoint.getBalance()).isEqualTo(expectedPoint.getBalance());

        verify(pointCoreRepository, times(1)).findByUserWithLock(user);
        verify(pointCoreRepository, times(1)).save(expectedPoint);
    }

    @DisplayName("Point 차감 - 성공")
    @Test
    void deductPoints() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        int amount = 1_000;
        Point point = new Point(1L, user, balance);
        Point expectedPoint = new Point(1L, user, balance - amount);

        when(pointCoreRepository.findByUserWithLock(user)).thenReturn(point);
        when(pointCoreRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointService.deductPoints(user, amount);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUser()).isEqualTo(expectedPoint.getUser());
        assertThat(savedPoint.getBalance()).isZero();

        verify(pointCoreRepository, times(1)).findByUserWithLock(user);
        verify(pointCoreRepository, times(1)).save(expectedPoint);
    }
}
