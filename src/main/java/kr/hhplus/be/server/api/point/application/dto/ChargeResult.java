package kr.hhplus.be.server.api.point.application.dto;

import kr.hhplus.be.server.domain.point.model.Point;

public record ChargeResult(
        long userId,
        int balance
) {
    public static ChargeResult from(Point point) {
        return new ChargeResult(
                point.getUserId(),
                point.getBalance()
        );
    }
}
