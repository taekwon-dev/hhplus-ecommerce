package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.api.order.application.OrderFacade;
import kr.hhplus.be.server.api.payment.application.dto.request.PaymentParam;
import kr.hhplus.be.server.api.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.model.BestSellingProduct;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import kr.hhplus.be.server.domain.product.model.SellableProduct;
import kr.hhplus.be.server.domain.product.repository.CategoryRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.dto.DeductStockParam;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.CategoryFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.when;

@DisplayName("상품 Service 통합 테스트")
class ProductServiceIntegrationTest extends ServiceTest {

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
    private OrderFacade orderFacade;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("ID 기반으로 상품을 조회한다.")
    @Test
    void findById() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        // when
        Product foundProduct = productService.findProductById(product.getId());

        // then
        assertThat(foundProduct.getName()).isEqualTo(product.getName());
        assertThat(foundProduct.getSalesPrice()).isEqualTo(product.getSalesPrice());
        assertThat(foundProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());
    }

    @DisplayName("ID 기반으로 상품 조회 시, 상품을 찾지 못한 경우 예외가 발생한다.")
    @Test
    void findById_doNotFound() {
        // given

        // when & then
        assertThatThrownBy(() -> productService.findProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @DisplayName("요청한 수량만큼 상품 재고를 차감한다.")
    @Test
    void deductStock() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 1));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        // when & then
        assertThatCode(() -> productService.deductStock(param))
                .doesNotThrowAnyException();
    }

    @DisplayName("요청한 수량만큼 상품 재고 차감 시, 재고가 0이된 경우 판매 가능한 상품 목록 캐시에서 제거한다.")
    @Test
    void deductStock_evictCache_whenOutOfStock() {
        // given
        String PRODUCT_SORTED_SET_KEY = "products:created_at:desc";
        String PRODUCT_HASH_PREFIX = "products:";

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 1));
        Pageable pageable = PageRequest.of(0, 10);

        productService.findSellableProducts(pageable);

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 1));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        // when & then
        assertThatCode(() -> productService.deductStock(param))
                .doesNotThrowAnyException();

        String productHashKey = PRODUCT_HASH_PREFIX + product.getId();
        Map<Object, Object> entry = redisTemplate.opsForHash().entries(productHashKey);
        assertThat(entry).isEmpty();

        int from = pageable.getPageNumber() * pageable.getPageSize();
        int to = from + pageable.getPageSize() - 1;
        Set<Object> zset = redisTemplate.opsForZSet().range(PRODUCT_SORTED_SET_KEY, from, to);
        assertThat(zset).doesNotContain(String.valueOf(product.getId()));
    }

    @DisplayName("요청한 수량만큼 상품 재고 차감 이후, 재고가 1보다 큰 경우 판매 가능한 상품 목록 캐시에서 유지된다.")
    @Test
    void deductStock_evictCache_whenStockIsPositive() {
        // given
        String PRODUCT_SORTED_SET_KEY = "products:created_at:desc";
        String PRODUCT_HASH_PREFIX = "products:";

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        Pageable pageable = PageRequest.of(0, 10);

        productService.findSellableProducts(pageable);

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 1));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        // when & then
        assertThatCode(() -> productService.deductStock(param))
                .doesNotThrowAnyException();

        String productHashKey = PRODUCT_HASH_PREFIX + product.getId();
        Map<Object, Object> entry = redisTemplate.opsForHash().entries(productHashKey);
        assertThat(entry).isNotEmpty();

        int from = pageable.getPageNumber() * pageable.getPageSize();
        int to = from + pageable.getPageSize() - 1;
        Set<Object> zset = redisTemplate.opsForZSet().range(PRODUCT_SORTED_SET_KEY, from, to);
        assertThat(zset).contains(String.valueOf(product.getId()));
    }

    @DisplayName("상품 재고 차감 시, 요청한 수량보다 상품의 재고가 부족한 경우 예외가 발생한다.")
    @Test
    void deductStock_insufficientStockQuantity() {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 5));

        List<DeductStockParam.Detail> deductStockParamDetails = List.of(new DeductStockParam.Detail(product.getId(), 10));
        DeductStockParam param = new DeductStockParam(deductStockParamDetails);

        // when & then
        assertThatThrownBy(() -> productService.deductStock(param))
                .isInstanceOf(InsufficientStockException.class);
    }

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
        List<SellableProduct> products = productService.findSellableProducts(pageable);

        // then
        assertThat(products).hasSize(3);
    }

    @DisplayName("판매 가능한 모든 상품 목록을 조회 시, 캐시 미스인 경우 캐시에 데이터를 업데이트한다.")
    @Test
    void findSellableProducts_whenCacheMiss() {
        // given
        String PRODUCT_SORTED_SET_KEY = "products:created_at:desc";
        String PRODUCT_HASH_PREFIX = "products:";

        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product1 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨1", category, 10_000, 50));
        Product product2 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨2", category, 10_000, 50));
        Product product3 = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨3", category, 10_000, 50));
        Pageable pageable = PageRequest.of(0, 10);

        productService.findSellableProducts(pageable);

        // when
        List<SellableProduct> products = productService.findSellableProducts(pageable);

        // then
        assertThat(products).hasSize(3);

        String productHashKey1 = PRODUCT_HASH_PREFIX + product1.getId();
        String productHashKey2 = PRODUCT_HASH_PREFIX + product2.getId();
        String productHashKey3 = PRODUCT_HASH_PREFIX + product3.getId();

        Map<Object, Object> entry1 = redisTemplate.opsForHash().entries(productHashKey1);
        Map<Object, Object> entry2 = redisTemplate.opsForHash().entries(productHashKey2);
        Map<Object, Object> entry3 = redisTemplate.opsForHash().entries(productHashKey3);

        assertThat(entry1).isNotEmpty();
        assertThat(entry2).isNotEmpty();
        assertThat(entry3).isNotEmpty();

        assertThat(entry1.get("name")).isEqualTo("라넌큘러스 오버핏 맨투맨1");
        assertThat(entry2.get("name")).isEqualTo("라넌큘러스 오버핏 맨투맨2");
        assertThat(entry3.get("name")).isEqualTo("라넌큘러스 오버핏 맨투맨3");

        int from = pageable.getPageNumber() * pageable.getPageSize();
        int to = from + pageable.getPageSize() - 1;
        Set<Object> zset = redisTemplate.opsForZSet().range(PRODUCT_SORTED_SET_KEY, from, to);
        assertThat(zset).contains(String.valueOf(product1.getId()));
        assertThat(zset).contains(String.valueOf(product2.getId()));
        assertThat(zset).contains(String.valueOf(product3.getId()));
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
            long orderId = orderFacade.order(user.getId(), createOrderParam);

            PaymentParam paymentParam = new PaymentParam(orderId, products.get(i - 1).getSalesPrice() * i, PaymentMethod.POINT_PAYMENT);
            paymentFacade.pay(user.getId(), paymentParam);
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
