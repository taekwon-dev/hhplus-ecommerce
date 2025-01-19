package kr.hhplus.be.server.api.order.controller.request;

import java.util.List;

public record OrderCreateRequest(
        List<OrderProductDetail> orderProductDetails
) {
}
