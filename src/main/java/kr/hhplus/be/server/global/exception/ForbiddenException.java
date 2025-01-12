package kr.hhplus.be.server.global.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ECommerceException {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    public ForbiddenException(String message) {
        super(message, STATUS);
    }
}
