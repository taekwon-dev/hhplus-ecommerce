package kr.hhplus.be.server.util.fixture;

import kr.hhplus.be.server.domain.product.model.Category;

public class CategoryFixture {

    public static Category create(String name) {
        return new Category(name);
    }

    public static Category create(String name, Category parent) {
        return new Category(name, parent);
    }
}
