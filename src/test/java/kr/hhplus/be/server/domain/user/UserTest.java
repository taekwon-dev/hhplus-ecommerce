package kr.hhplus.be.server.domain.user;

import kr.hhplus.be.server.domain.user.domain.Address;
import kr.hhplus.be.server.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @DisplayName("유저를 생성한다.")
    @Test
    void createUser() {
        // given
        String username = "test_username";
        Address address = new Address("addressLine1", "addressLine2", "00000");

        // when
        User user = new User(username, address);

        // then
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getAddressLine1()).isEqualTo(address.getAddressLine1());
        assertThat(user.getAddressLine2()).isEqualTo(address.getAddressLine2());
        assertThat(user.getPostalCode()).isEqualTo(address.getPostalCode());
    }
}
