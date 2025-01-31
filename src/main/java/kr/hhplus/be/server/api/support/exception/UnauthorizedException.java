package kr.hhplus.be.server.api.support.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ECommerceException {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(message, STATUS);
    }
}
