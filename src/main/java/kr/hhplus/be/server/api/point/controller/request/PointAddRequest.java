package kr.hhplus.be.server.api.point.controller.request;

public record PointAddRequest(
        Long userId,
        Integer amount
) {
}
