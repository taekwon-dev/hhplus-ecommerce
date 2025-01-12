package kr.hhplus.be.server.unit.api.point.facade;

import kr.hhplus.be.server.api.point.controller.request.PointAddRequest;
import kr.hhplus.be.server.api.point.controller.request.PointDeductRequest;
import kr.hhplus.be.server.api.point.controller.response.PointResponse;
import kr.hhplus.be.server.api.point.facade.PointFacade;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.domain.PointTransaction;
import kr.hhplus.be.server.domain.point.domain.PointTransactionType;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.service.PointTransactionService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.service.UserService;
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
class PointFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private PointService pointService;

    @Mock
    private PointTransactionService pointTransactionService;

    @InjectMocks
    private PointFacade pointFacade;

    @DisplayName("Point User 기반 조회 - 성공")
    @Test
    void findPointByUser() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        Point point = new Point(1L, user, balance);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(pointService.findPointByUser(user)).thenReturn(point);

        // when
        PointResponse response = pointFacade.getPointBalance(user.getId());

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isEqualTo(balance);

        verify(userService, times(1)).findUserById(user.getId());
        verify(pointService, times(1)).findPointByUser(user);
    }

    @DisplayName("Point 추가 - 성공")
    @Test
    void addPoints() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 0;
        int amount = 1_000;
        Point expectedPoint = new Point(1L, user, balance + amount);
        PointAddRequest request = new PointAddRequest(user.getId(), amount);
        PointTransactionType type = PointTransactionType.CHARGE;
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(pointService.addPoints(user, amount)).thenReturn(expectedPoint);
        when(pointTransactionService.recordPointTransaction(user, amount, type)).thenReturn(pointTransaction);

        // when
        PointResponse response = pointFacade.addPoints(request);

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isEqualTo(expectedPoint.getBalance());

        verify(userService, times(1)).findUserById(user.getId());
        verify(pointService, times(1)).addPoints(user, amount);
        verify(pointTransactionService, times(1)).recordPointTransaction(user, amount, type);
    }

    @DisplayName("Point 차감 - 성공")
    @Test
    void deductPoints() {
        // given
        User user = UserFixture.USER(1L);
        int balance = 1_000;
        int amount = 1_000;
        Point expectedPoint = new Point(1L, user, balance - amount);
        PointDeductRequest request = new PointDeductRequest(user.getId(), amount);
        PointTransactionType type = PointTransactionType.USAGE;
        PointTransaction pointTransaction = new PointTransaction(user, amount, type);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(pointService.deductPoints(user, amount)).thenReturn(expectedPoint);
        when(pointTransactionService.recordPointTransaction(user, amount, type)).thenReturn(pointTransaction);

        // when
        PointResponse response = pointFacade.deductPoints(request);

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.balance()).isEqualTo(expectedPoint.getBalance());

        verify(userService, times(1)).findUserById(user.getId());
        verify(pointService, times(1)).deductPoints(user, amount);
        verify(pointTransactionService, times(1)).recordPointTransaction(user, amount, type);
    }
}
