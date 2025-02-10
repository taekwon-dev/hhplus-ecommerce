package kr.hhplus.be.server.api.product.application;

import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.api.product.application.dto.BestSellingProductsResult;
import kr.hhplus.be.server.api.product.application.dto.SellableProductsResult;
import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("상품 Facade 통합 테스트")
class ProductFacadeTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ProductFacade productFacade;

    @MockitoBean
    private Clock clock;

    @DisplayName("판매 가능한 모든 상품 목록을 조회한다.")
    @Test
    void findSellableProducts() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SellableProductsResult result = productFacade.findSellableProducts(pageable);

        // then
        assertThat(result.products()).hasSize(3);
        assertThat(result.products()).extracting(SellableProductsResult.ProductDetail::name)
                .containsExactlyInAnyOrder("라넌큘러스 오버핏 맨투맨1", "라넌큘러스 오버핏 맨투맨2", "라넌큘러스 오버핏 맨투맨3");
    }

    @DisplayName("지난 3일 동안 가장 많이 팔린 상위 5개 상품 목록을 조회한다.")
    @Test
    void findBestSellingProducts() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product1 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        Product product2 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        Product product3 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Product product4 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨4", category, 10_000, 50));
        Product product5 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨5", category, 10_000, 50));
        Product product6 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨6", category, 10_000, 50));
        List<Product> products = List.of(product1, product2, product3, product4, product5, product6);
        Pageable pageable = PageRequest.ofSize(5);

        int initialBalance = 1_000_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        for (int i = 1; i <= products.size(); i++) {
            List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(products.get(i - 1).getId(), products.get(i - 1).getSalesPrice(), i));
            CreateOrderParam createOrderParam = new CreateOrderParam(createOrderParamDetails);
            long savedOrderId = orderFacade.order(user.getId(), createOrderParam);

            PaymentParam paymentParam = new PaymentParam(savedOrderId, products.get(i - 1).getSalesPrice(), PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(user.getId(), paymentParam);
        }

        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when
        BestSellingProductsResult result = productFacade.findBestSellingProducts(pageable);

        // then
        assertThat(result.products()).hasSize(5);
        assertThat(result.products()).noneMatch(response -> response.productId() == 1);
    }
}
