package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.user.domain.Address;
import kr.hhplus.be.server.domain.user.domain.User;

public class UserFixture {

    public static User USER() {
        return new User("username", new Address("서울특별시 강남구 테헤란로 123", "강남빌딩 10층", "06230"));
    }

    public static User USER(Long id) {
        return new User(id, "username", new Address("서울특별시 강남구 테헤란로 123", "강남빌딩 10층", "06230"));
    }
}
