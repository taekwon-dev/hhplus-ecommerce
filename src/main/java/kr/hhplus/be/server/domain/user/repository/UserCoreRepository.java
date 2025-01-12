package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCoreRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User findById(long id) {
        return jpaRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
}
