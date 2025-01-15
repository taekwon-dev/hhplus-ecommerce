package kr.hhplus.be.server.domain.auth.exception;

import kr.hhplus.be.server.global.exception.UnauthorizedException;

public class IllegalTokenException extends UnauthorizedException {

    private static final String MESSAGE = "유효하지 않는 토큰입니다.";

    public IllegalTokenException() {
        super(MESSAGE);
    }
}
