package kr.hhplus.be.server.unit.domain.product.repository;

import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.ProductCoreRepository;
import kr.hhplus.be.server.domain.product.repository.ProductJpaRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.ProductFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static kr.hhplus.be.server.domain.order.domain.OrderStatus.DELIVERED;
import static kr.hhplus.be.server.domain.order.domain.OrderStatus.PAYMENT_COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCoreRepositoryTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @InjectMocks
    private ProductCoreRepository productCoreRepository;

    @DisplayName("Product 저장 - 성공")
    @Test
    void saveProduct() {
        // given
        String name = "라넌큘러스 오버핏 맨투맨";
        Category category = CategoryFixture.create("상의");
        int price = 12_000;
        int stockQuantity = 100;
        Product product = new Product(name, category, price, stockQuantity);

        when(productJpaRepository.save(product)).thenReturn(product);

        // when
        Product savedProduct = productCoreRepository.save(product);

        // then
        assertThat(savedProduct.getName()).isEqualTo(product.getName());
        assertThat(savedProduct.getCategory()).isEqualTo(product.getCategory());
        assertThat(savedProduct.getPrice()).isEqualTo(product.getPrice());
        assertThat(savedProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());

        verify(productJpaRepository, times(1)).save(product);
    }

    @DisplayName("모든 Product 목록 조회 - 성공")
    @Test
    void findAllProducts() {
        // given
        Product product1 = ProductFixture.create(1L, 1_000, 10);
        Product product2 = ProductFixture.create(2L, 1_000, 10);
        Product product3 = ProductFixture.create(3L, 1_000, 10);
        Pageable pageable = PageRequest.of(0, 10);

        when(productJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product1, product2, product3)));

        // when
        Page<Product> products = productCoreRepository.findAllProducts(pageable);

        // then
        assertThat(products).hasSize(3);
        assertThat(products).contains(product1, product2, product3);

        verify(productJpaRepository, times(1)).findAll(pageable);
    }

    @DisplayName("가장 많이 팔린 상위 5개 Product 조회 - 성공")
    @Test
    void findBestSellingProducts() {
        // given
        Product product = ProductFixture.create(1L, 1_000, 10);
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.minusDays(3);
        List<OrderStatus> statuses = List.of(PAYMENT_COMPLETED, DELIVERED);
        Pageable pageable = PageRequest.ofSize(5);
        BestSellingProduct bestSellingProduct = new BestSellingProduct(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity(), 1);

        User user = UserFixture.USER(1L);
        Order order = new Order(user);
        order.addOrderProduct(product, 1);

        when(productJpaRepository.findBestSellingProducts(startDateTime, endDateTime, pageable, statuses)).thenReturn(List.of(bestSellingProduct));

        // when
        List<BestSellingProduct> bestSellingProducts = productCoreRepository.findBestSellingProducts(startDateTime, endDateTime, pageable);

        // then
        assertThat(bestSellingProducts).hasSize(1);
        assertThat(bestSellingProducts).contains(bestSellingProduct);

        verify(productJpaRepository, times(1)).findBestSellingProducts(startDateTime, endDateTime, pageable, statuses);
    }
}
