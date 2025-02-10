package kr.hhplus.be.server.api.order.application;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.api.order.application.dto.request.CreateOrderParam;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderStatus;
import kr.hhplus.be.server.domain.product.exception.ProductNotFoundException;
import kr.hhplus.be.server.domain.product.model.Category;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("주문 Facade 통합 테스트")
class OrderFacadeTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("주문 정보를 저장한다.")
    @Test
    void order() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 10));
        int quantity = 1;

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), quantity));
        CreateOrderParam param = new CreateOrderParam(createOrderParamDetails);

        // when
        long savedOrderId = orderFacade.order(user.getId(), param);

        // then
        Order foundOrder = entityManager.createQuery(
                        "SELECT o FROM Order o WHERE o.id = :id", Order.class)
                .setParameter("id", savedOrderId)
                .getSingleResult();

        assertThat(foundOrder.getUserId()).isEqualTo(user.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(foundOrder.getOrderPrice()).isEqualTo(product.getSalesPrice() * quantity);
    }

    @DisplayName("주문 시, 상품 재고가 부족한 경우 예외가 발생한다.")
    @Test
    void order_insufficientStockQuantity() {
        // given
        int stockQuantity = 1;
        int orderQuantity = 10;

        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, stockQuantity));

        List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), orderQuantity));
        CreateOrderParam param = new CreateOrderParam(createOrderParamDetails);

        // when & then
        assertThatThrownBy(() -> orderFacade.order(user.getId(), param))
                .isInstanceOf(InsufficientStockException.class);
    }

    /**
     * 30명이 동시에 상품 주문을 시도하고, 상품 재고가 떨어진 경우 예외 발생을 검증하는 테스트입니다.
     * 각 유저는 1개씩 주문, 상품 재고는 20개로 가정합니다. (성공: 20명, 실패: 10명)
     */
    @DisplayName("재고가 20개인 상품을 30명의 서로 다른 유저가 한 개씩 주문 할 때, 10명은 해당 상품 주문에 실패한다.")
    @Test
    void orderConcurrently_FailureAfterExceededStockQuantity() throws InterruptedException {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 20));

        int threads = 30;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<User> users = new ArrayList<>();
        List<CreateOrderParam> params = new ArrayList<>();
        for (int i = 1; i <= threads; i++) {
            User user = userRepository.save(UserFixture.USER());
            users.add(user);

            List<CreateOrderParam.Detail> createOrderParamDetails = List.of(new CreateOrderParam.Detail(product.getId(), product.getSalesPrice(), 1));
            CreateOrderParam param = new CreateOrderParam(createOrderParamDetails);
            params.add(param);
        }

        // when
        executeConcurrency(threads, idx -> {
            try {
                orderFacade.order(users.get(idx).getId(), params.get(idx));
                successCount.incrementAndGet();
            } catch (InsufficientStockException e) {
                failCount.incrementAndGet();
            }
        });

        // then
        assertThat(successCount.get()).isEqualTo(20);
        assertThat(failCount.get()).isEqualTo(10);

        Product foundProduct = productRepository.findById(product.getId()).orElseThrow(ProductNotFoundException::new);
        assertThat(foundProduct.getStockQuantity()).isZero();
    }

    private void executeConcurrency(int threads, Consumer<Integer> task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            int idx = i;
            executor.execute(() -> {
                try {
                    task.accept(idx);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }
}
