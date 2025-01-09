package kr.hhplus.be.server.domain.order.exception;

import kr.hhplus.be.server.global.exception.BadRequestException;

public class InsufficientStockException extends BadRequestException {

    private static final String message = "상품 재고가 부족하여 주문 할 수 없습니다.";

    public InsufficientStockException() {
        super(message);
    }
}
