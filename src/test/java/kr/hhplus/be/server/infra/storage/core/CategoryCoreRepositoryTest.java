package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.CategoryJpaRepository;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryCoreRepositoryTest {

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @InjectMocks
    private CategoryCoreRepository categoryCoreRepository;

    @DisplayName("카테고리를 저장한다.")
    @Test
    void saveCategory() {
        // given
        Category parent = CategoryFixture.create("상의");
        Category category = CategoryFixture.create("맨투맨", parent);

        when(categoryJpaRepository.save(category)).thenReturn(category);

        // when
        Category savedCategory = categoryCoreRepository.save(category);

        // then
        assertThat(savedCategory.getName()).isEqualTo(category.getName());
        assertThat(savedCategory.getParent()).isEqualTo(category.getParent());

        verify(categoryJpaRepository, times(1)).save(category);
    }
}
