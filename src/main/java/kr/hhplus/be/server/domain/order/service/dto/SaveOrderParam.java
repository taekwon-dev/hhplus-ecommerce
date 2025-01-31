package kr.hhplus.be.server.domain.order.service.dto;

import java.util.List;

public record SaveOrderParam(
        List<SaveOrderParam.Detail> saveOrderParamDetails
) {
    public record Detail(
            Long productId,
            Integer salesPrice,
            Integer quantity
    ) {
    }
}
