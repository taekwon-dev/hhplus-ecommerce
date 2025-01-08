package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.domain.User;

public interface UserRepository {

    User save(User member);

    User findById(long id);
}
