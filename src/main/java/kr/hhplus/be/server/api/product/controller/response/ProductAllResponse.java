package kr.hhplus.be.server.api.product.controller.response;

import java.util.List;

public record ProductAllResponse(
        List<ProductResponse> products,
        int currentPage,
        int totalPages,
        int pageSize
) {
}
