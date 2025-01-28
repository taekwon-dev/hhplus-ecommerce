package kr.hhplus.be.server.unit.domain.user.repository;

import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.exception.UserNotFoundException;
import kr.hhplus.be.server.domain.user.repository.UserCoreRepository;
import kr.hhplus.be.server.domain.user.repository.UserJpaRepository;
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
class UserCoreRepositoryTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserCoreRepository userCoreRepository;

    @DisplayName("유저를 저장한다.")
    @Test
    void saveUser() {
        // given
        User user = UserFixture.USER();
        User expectedUser = UserFixture.USER(1L);
        when(userJpaRepository.save(user)).thenReturn(expectedUser);

        // when
        User savedUser = userCoreRepository.save(user);

        // then
        assertThat(savedUser).isEqualTo(expectedUser);
        assertThat(savedUser.getId()).isEqualTo(expectedUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo(expectedUser.getUsername());
        assertThat(savedUser.getAddressLine1()).isEqualTo(expectedUser.getAddressLine1());
        assertThat(savedUser.getAddressLine2()).isEqualTo(expectedUser.getAddressLine2());
        assertThat(savedUser.getPostalCode()).isEqualTo(expectedUser.getPostalCode());

        verify(userJpaRepository, times(1)).save(user);
    }

    @DisplayName("ID 기반으로 유저를 조회한다.")
    @Test
    void findById() {
        // given
        User user = UserFixture.USER();
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        User foundUser = userCoreRepository.findById(1L);

        // then
        assertThat(foundUser).isEqualTo(user);
        assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(foundUser.getAddressLine1()).isEqualTo(user.getAddressLine1());
        assertThat(foundUser.getAddressLine2()).isEqualTo(user.getAddressLine2());
        assertThat(foundUser.getPostalCode()).isEqualTo(user.getPostalCode());

        verify(userJpaRepository, times(1)).findById(1L);
    }

    @DisplayName("ID 기반으로 유저 조회 시, 대상 유저를 찾지 못한 경우 예외 발생한다.")
    @Test
    void findById_doNotExist() {
        // given
        when(userJpaRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userCoreRepository.findById(1L))
            .isInstanceOf(UserNotFoundException.class);

        verify(userJpaRepository, times(1)).findById(1L);
    }
}
