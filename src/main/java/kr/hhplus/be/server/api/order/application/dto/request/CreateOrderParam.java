package kr.hhplus.be.server.api.order.application.dto.request;

import kr.hhplus.be.server.domain.order.service.dto.SaveOrderParam;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;

import java.util.List;

public record CreateOrderParam(
        List<CreateOrderParam.Detail> createOrderParamDetails
) {
    public record Detail(
            Long productId,
            Integer salesPrice,
            Integer quantity
    ) {
    }

    public DeductStockParam toDeductStockParam() {
        List<DeductStockParam.Detail> deductStockDetails = createOrderParamDetails.stream()
                .map(detail -> new DeductStockParam.Detail(detail.productId, detail.quantity))
                .toList();
        return new DeductStockParam(deductStockDetails);
    }

    public SaveOrderParam toSaveOrderParam() {
        List<SaveOrderParam.Detail> saveOrderParamDetails = createOrderParamDetails.stream()
                .map(detail -> new SaveOrderParam.Detail(detail.productId, detail.salesPrice, detail.quantity))
                .toList();
        return new SaveOrderParam(saveOrderParamDetails);
    }
}
