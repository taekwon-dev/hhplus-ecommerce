package kr.hhplus.be.server.api.order.controller.dto.request;

import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderRequest.Detail> createOrderRequestDetails
) {
    public record Detail(
            Long productId,
            Integer salesPrice,
            Integer quantity
    ) {
        public CreateOrderParam.Detail toCreateOrderParamDetail() {
            return new CreateOrderParam.Detail(productId, salesPrice ,quantity);
        }
    }

    public CreateOrderParam toCreateOrderParam() {
        return new CreateOrderParam(
                createOrderRequestDetails.stream()
                        .map(CreateOrderRequest.Detail::toCreateOrderParamDetail)
                        .toList()
        );
    }
}
