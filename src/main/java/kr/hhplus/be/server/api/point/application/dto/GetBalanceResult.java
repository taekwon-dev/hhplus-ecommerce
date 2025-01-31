package kr.hhplus.be.server.api.point.application.dto;

import kr.hhplus.be.server.domain.point.model.Point;

public record GetBalanceResult(
        long userId,
        int balance
) {
    public static GetBalanceResult from(Point point) {
        return new GetBalanceResult(
                point.getUserId(),
                point.getBalance()
        );
    }
}
