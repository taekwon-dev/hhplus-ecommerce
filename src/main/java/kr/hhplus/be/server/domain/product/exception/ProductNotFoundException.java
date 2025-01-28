package kr.hhplus.be.server.domain.product.exception;

import kr.hhplus.be.server.api.support.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {

    private static final String message = "해당 상품을 찾을 수 없습니다.";

    public ProductNotFoundException() {
        super(message);
    }
}
