package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;

public class ProductFixture {

    public static Product create(Long id, int price, int stockQuantity) {
        return new Product(id, "product " + id, new Category("category"), price, stockQuantity);
    }
}
