package kr.hhplus.be.server.unit.domain.point.repository;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointCoreRepository;
import kr.hhplus.be.server.domain.point.repository.PointJpaRepository;
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

    @DisplayName("Point 저장 - 성공")
    @Test
    void savePoint() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        Point point = new Point(user, balance);
        Point expectedPoint = new Point(1L, user, balance);

        when(pointJpaRepository.save(point)).thenReturn(expectedPoint);

        // when
        Point savedPoint = pointCoreRepository.save(point);

        // then
        assertThat(savedPoint).isEqualTo(expectedPoint);
        assertThat(savedPoint.getId()).isEqualTo(expectedPoint.getId());
        assertThat(savedPoint.getUser()).isEqualTo(expectedPoint.getUser());
        assertThat(savedPoint.getBalance()).isEqualTo(expectedPoint.getBalance());

        verify(pointJpaRepository, times(1)).save(point);
    }

    @DisplayName("Point User 기반 조회 - 성공")
    @Test
    void findByUser() {
        // given
        User user = UserFixture.USER();
        int balance = 1_000;
        Point point = new Point(1L, user, balance);

        when(pointJpaRepository.findPointByUser(user)).thenReturn(Optional.of(point));

        // when
        Point foundPoint = pointCoreRepository.findByUser(user);

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUser()).isEqualTo(point.getUser());
        assertThat(foundPoint.getBalance()).isEqualTo(point.getBalance());

        verify(pointJpaRepository, times(1)).findPointByUser(user);
    }

    @DisplayName("Point User 기반 조회 - 성공 - 포인트 정보를 찾지 못한 경우 0으로 초기화 한 뒤 반환")
    @Test
    void findByUser_Initialize_Balance() {
        // given
        User user = UserFixture.USER();
        int balance = 0;
        Point point = new Point(1L, user, balance);

        when(pointJpaRepository.findPointByUser(user)).thenReturn(Optional.of(point));

        // when
        Point foundPoint = pointCoreRepository.findByUser(user);

        // then
        assertThat(foundPoint).isEqualTo(point);
        assertThat(foundPoint.getId()).isEqualTo(point.getId());
        assertThat(foundPoint.getUser()).isEqualTo(point.getUser());
        assertThat(foundPoint.getBalance()).isZero();

        verify(pointJpaRepository, times(1)).findPointByUser(user);
    }
}
