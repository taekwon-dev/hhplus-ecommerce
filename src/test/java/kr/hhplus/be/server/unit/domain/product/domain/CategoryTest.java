package kr.hhplus.be.server.unit.domain.product.domain;

import kr.hhplus.be.server.domain.product.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @DisplayName("Category 생성 - 성공")
    @Test
    void createCategory() {
        // given
        String name = "맨투맨";

        // when
        Category category = new Category(name);

        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getParent()).isNull();
    }

    @DisplayName("Category 생성 및 상위 카테고리 지정 - 성공")
    @Test
    void createCategory_withParentCategory() {
        // given
        String parentName = "상의";
        String name = "맨투맨";
        Category parent = new Category(parentName);

        // when
        Category category = new Category(name, parent);

        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getParent()).isEqualTo(parent);
    }
}
