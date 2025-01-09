package kr.hhplus.be.server.unit.domain.user.service;

import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.exception.UserNotFoundException;
import kr.hhplus.be.server.domain.user.repository.UserCoreRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserCoreRepository userCoreRepository;

    @InjectMocks
    private UserService userService;

    @DisplayName("User ID 기반 조회 - 성공")
    @Test
    void findUserById() {
        // given
        User user = UserFixture.USER();
        when(userService.findUserById(1L)).thenReturn(user);

        // when
        User foundUser = userService.findUserById(1L);

        // then
        assertThat(foundUser).isEqualTo(user);
        assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(foundUser.getAddressLine1()).isEqualTo(user.getAddressLine1());
        assertThat(foundUser.getAddressLine2()).isEqualTo(user.getAddressLine2());
        assertThat(foundUser.getPostalCode()).isEqualTo(user.getPostalCode());

        verify(userCoreRepository, times(1)).findById(1L);
    }
}
