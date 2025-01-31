package kr.hhplus.be.server.domain.user.exception;

import kr.hhplus.be.server.api.support.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    private static final String message = "해당 유저를 찾을 수 없습니다.";

    public UserNotFoundException() {
        super(message);
    }
}
