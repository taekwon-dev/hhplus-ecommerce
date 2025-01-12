package kr.hhplus.be.server.domain.product.exception;

import kr.hhplus.be.server.global.exception.BadRequestException;

public class InsufficientStockException extends BadRequestException {

    private static final String message = "상품 재고가 부족합니다.";

    public InsufficientStockException() {
        super(message);
    }
}
