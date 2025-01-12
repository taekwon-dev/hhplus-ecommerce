package kr.hhplus.be.server.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ECommerceException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public BadRequestException(String message) {
        super(message, STATUS);
    }
}
