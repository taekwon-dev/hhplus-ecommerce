package kr.hhplus.be.server.api.point.controller.request;

public record PointDeductRequest(
        Long userId,
        Integer amount
) {
}
