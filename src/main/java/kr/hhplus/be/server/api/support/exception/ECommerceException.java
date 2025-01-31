package kr.hhplus.be.server.api.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ECommerceException extends RuntimeException {

    private final HttpStatus status;

    public ECommerceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
