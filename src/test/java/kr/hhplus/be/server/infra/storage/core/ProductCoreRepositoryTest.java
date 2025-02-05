package kr.hhplus.be.server.infra.storage.core;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderProduct;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.ProductJpaRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static kr.hhplus.be.server.domain.order.model.OrderStatus.DELIVERED;
import static kr.hhplus.be.server.domain.order.model.OrderStatus.PAYMENT_COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCoreRepositoryTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @InjectMocks
    private ProductCoreRepository productCoreRepository;

    @DisplayName("상품을 저장한다.")
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
        assertThat(savedProduct.getSalesPrice()).isEqualTo(product.getSalesPrice());
        assertThat(savedProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());

        verify(productJpaRepository, times(1)).save(product);
    }

    @DisplayName("판매 가능한 모든 상품 목록을 조회한다.")
    @Test
    void findAllProducts() {
        // given
        SellableProduct product1 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product2 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        SellableProduct product3 = new SellableProduct(1L, "상의", "라넌큘러스 오버핏 맨투맨", 10_000, 10, LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);

        when(productJpaRepository.findSellableProducts(pageable)).thenReturn(List.of(product1, product2, product3));

        // when
        List<SellableProduct> products = productCoreRepository.findSellableProducts(pageable);

        // then
        assertThat(products).hasSize(3);
        assertThat(products).contains(product1, product2, product3);

        verify(productJpaRepository, times(1)).findSellableProducts(pageable);
    }

    @DisplayName("지난 3일 동안 가장 많이 팔린 상위 5개 상품 목록을 조회한다.")
    @Test
    void findBestSellingProducts() {
        // given
        Product product = ProductFixture.create(1L, 1_000, 10);
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.minusDays(3);
        List<OrderStatus> statuses = List.of(PAYMENT_COMPLETED, DELIVERED);
        Pageable pageable = PageRequest.ofSize(5);
        BestSellingProduct bestSellingProduct = new BestSellingProduct(product.getId(), product.getName(), product.getSalesPrice(), product.getStockQuantity(), 1);

        User user = UserFixture.USER(1L);
        Order order = new Order(1L, user.getId());
        OrderProduct orderProduct = new OrderProduct(order.getId(), product.getId(), product.getSalesPrice(), 1);
        order.addOrderProduct(orderProduct);

        when(productJpaRepository.findBestSellingProducts(startDateTime, endDateTime, pageable, statuses)).thenReturn(List.of(bestSellingProduct));

        // when
        List<BestSellingProduct> bestSellingProducts = productCoreRepository.findBestSellingProducts(startDateTime, endDateTime, pageable);

        // then
        assertThat(bestSellingProducts).hasSize(1);
        assertThat(bestSellingProducts).contains(bestSellingProduct);

        verify(productJpaRepository, times(1)).findBestSellingProducts(startDateTime, endDateTime, pageable, statuses);
    }
}
