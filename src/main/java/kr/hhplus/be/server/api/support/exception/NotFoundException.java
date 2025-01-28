package kr.hhplus.be.server.api.support.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ECommerceException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public NotFoundException(String message) {
        super(message, STATUS);
    }
}
