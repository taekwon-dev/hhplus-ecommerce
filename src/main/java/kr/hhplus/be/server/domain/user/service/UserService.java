package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id);
    }
}
