package kr.hhplus.be.server.api.point.controller.dto.response;

import kr.hhplus.be.server.api.point.application.dto.GetBalanceResult;

public record GetBalanceResponse(
        long userId,
        int balance
) {
    public static GetBalanceResponse from(GetBalanceResult result) {
        return new GetBalanceResponse(
                result.userId(),
                result.balance()
        );
    }
}
