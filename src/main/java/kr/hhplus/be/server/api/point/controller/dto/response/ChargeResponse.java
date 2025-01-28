package kr.hhplus.be.server.api.point.controller.dto.response;

import kr.hhplus.be.server.api.point.application.dto.ChargeResult;

public record ChargeResponse(
        long userId,
        int balance
) {
    public static ChargeResponse from(ChargeResult result) {
        return new ChargeResponse(
                result.userId(),
                result.balance()
        );
    }
}
