package kr.hhplus.be.server.integration.domain.product.service;

import kr.hhplus.be.server.api.order.controller.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.controller.request.OrderProductDetail;
import kr.hhplus.be.server.domain.order.facade.OrderFacade;
import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.domain.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.domain.BestSellingProduct;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.ProductQuantityDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProductServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private OrderFacade orderFacade;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("ID 기반으로 상품을 조회한다.")
    @Test
    void findById() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        // when
        Product foundProduct = productService.findById(product.getId());

        // then
        assertThat(foundProduct.getName()).isEqualTo(product.getName());
        assertThat(foundProduct.getPrice()).isEqualTo(product.getPrice());
        assertThat(foundProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());
    }

    @DisplayName("ID 기반으로 상품 조회 시, 상품을 찾지 못한 경우 예외가 발생한다.")
    @Test
    void findById_doNotFound() {
        // given

        // when & then
        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @DisplayName("주문 수량 만큼 상품 재고가 충분한 경우 검증에 성공한다.")
    @Test
    void validateStock_withSufficientStock() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        // when & then
        assertThatCode(() -> productService.validateStock(productQuantityDtos))
                .doesNotThrowAnyException();
    }

    @DisplayName("주문 수량 만큼 상품 재고가 충분하지 않은 경우 예외가 발생한다.")
    @Test
    void validateStock_withInsufficientStock() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 5));

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), 10);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        // when & then
        assertThatThrownBy(() -> productService.validateStock(productQuantityDtos))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("요청한 수량만큼 상품 재고를 차감한다.")
    @Test
    void deductStock() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), 5);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        // when & then
        assertThatCode(() -> productService.deductStock(productQuantityDtos))
                .doesNotThrowAnyException();
    }

    @DisplayName("상품 재고 차감 시, 요청한 수량보다 상품의 재고가 부족한 경우 예외가 발생한다.")
    @Test
    void deductStock_insufficientStockQuantity() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 5));

        ProductQuantityDto productQuantityDto = new ProductQuantityDto(product.getId(), 10);
        List<ProductQuantityDto> productQuantityDtos = List.of(productQuantityDto);

        // when & then
        assertThatThrownBy(() -> productService.deductStock(productQuantityDtos))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("판매 가능한 모든 상품 목록을 조회한다.")
    @Test
    void findAllProducts() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product1 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        Product product2 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        Product product3 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> products = productService.findAllProducts(pageable);

        // then
        assertThat(products).hasSize(3);
        assertThat(products).contains(product1, product2, product3);
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
        pointRepository.save(new Point(user, initialBalance));

        for (int i = 1; i <= products.size(); i++) {
            OrderProductDetail orderProductDetail = new OrderProductDetail(products.get(i - 1).getId(), i);
            List<OrderProductDetail> orderProductDetails = List.of(orderProductDetail);
            OrderCreateRequest request = new OrderCreateRequest(orderProductDetails);
            long orderId = orderFacade.order(user, request);
            PaymentRequest paymentRequest = new PaymentRequest(orderId, PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(user, paymentRequest);
        }

        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when
        List<BestSellingProduct> bestSellingProducts = productService.findBestSellingProducts(pageable);

        // then
        assertThat(bestSellingProducts).hasSize(5);
        assertThat(bestSellingProducts).noneMatch(product -> product.productId() == 1);
    }
}
