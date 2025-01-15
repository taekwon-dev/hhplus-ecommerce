package kr.hhplus.be.server.integration.api.product.facade;

import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.controller.request.OrderProductDetail;
import kr.hhplus.be.server.api.order.facade.OrderFacade;
import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.facade.PaymentFacade;
import kr.hhplus.be.server.api.product.controller.response.BestSellingProductResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductAllResponse;
import kr.hhplus.be.server.api.product.controller.response.ProductResponse;
import kr.hhplus.be.server.api.product.facade.ProductFacade;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProductFacadeTest {

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

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("Product 모든 목록 조회 - 성공")
    @Test
    void findAllProducts() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        ProductAllResponse response = productFacade.findAllProducts(pageable);

        // then
        assertThat(response.products()).hasSize(3);
        assertThat(response.products()).extracting(ProductResponse::name)
                .containsExactlyInAnyOrder("라넌큘러스 오버핏 맨투맨1", "라넌큘러스 오버핏 맨투맨2", "라넌큘러스 오버핏 맨투맨3");
    }

    @DisplayName("가장 많이 팔린 상위 5개 Product 조회 - 성공")
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
        pointRepository.save(new Point(user, initialBalance));

        for (int i = 1; i <= products.size(); i++) {
            OrderProductDetail orderProductDetail = new OrderProductDetail(products.get(i - 1).getId(), i);
            List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
            OrderCreateRequest request = new OrderCreateRequest(user.getId(), orderProductDetails);
            long orderId = orderFacade.order(request);
            PaymentRequest paymentRequest = new PaymentRequest(user.getId(), orderId, PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(paymentRequest);
        }

        // when
        List<BestSellingProductResponse> responses = productFacade.findBestSellingProducts(pageable);

        // then
        assertThat(responses).hasSize(5);
        assertThat(responses).noneMatch(response -> response.productId() == 1);
    }
}
