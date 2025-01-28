package kr.hhplus.be.server.domain.product.service.dto;

import java.util.List;

public record DeductStockParam (
        List<DeductStockParam.Detail> deductStockParamDetails
) {
    public record Detail(
            long productId,
            int quantity
    ) {
    }
}
