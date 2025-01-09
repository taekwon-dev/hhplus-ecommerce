package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.domain.Product;

import java.util.List;

public interface ProductRepository {

    Product save(Product product);

    // 상품 목록 조회 (페이징)
}
