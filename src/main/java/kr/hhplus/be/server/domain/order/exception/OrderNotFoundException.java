package kr.hhplus.be.server.domain.order.exception;

import kr.hhplus.be.server.global.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    private static final String message = "해당 주문 정보를 찾을 수 없습니다.";

    public OrderNotFoundException() {
        super(message);
    }
}
