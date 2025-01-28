package kr.hhplus.be.server.integration.domain.payment.facade;

import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.response.PaymentResponse;
import kr.hhplus.be.server.domain.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderStatus;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.payment.domain.PaymentMethod;
import kr.hhplus.be.server.domain.payment.domain.PaymentStatus;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.product.domain.Category;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.exception.InsufficientStockException;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentFacadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("결제를 한다.")
    @Test
    void pay() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 10_000, 10));

        int quantity = 1;
        Order order = new Order(user);
        order.addOrderProduct(product, quantity);
        orderRepository.save(order);

        int initialBalance = 50_000;
        pointRepository.save(new Point(user, initialBalance));

        PaymentRequest request = new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT);

        // when
        PaymentResponse response = paymentFacade.pay(user, request);

        // then
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.totalPrice()).isEqualTo(order.calculateTotalPrice());
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.POINT_PAYMENT);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);

        Order foundOrder = orderRepository.findById(order.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        Product foundProduct = productRepository.findById(product.getId());
        assertThat(foundProduct.getStockQuantity()).isEqualTo(9);
    }

    @DisplayName("결제 시 상품 재고가 부족 시, 예외가 발생한다.")
    @Test
    void pay_Fail_InsufficientStockQuantity() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 10_000, 1));

        int quantity = 2;
        Order order = new Order(user);
        order.addOrderProduct(product, quantity);
        orderRepository.save(order);

        int initialBalance = 50_000;
        pointRepository.save(new Point(user, initialBalance));

        PaymentRequest request = new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT);

        // when & then
        assertThatThrownBy(() -> paymentFacade.pay(user, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("결제 시 포인트 잔액 부족 시, 예외가 발생한다.")
    @Test
    void pay_Fail_InsufficientBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 10_000, 10));

        int quantity = 2;
        Order order = new Order(user);
        order.addOrderProduct(product, quantity);
        orderRepository.save(order);

        int initialBalance = 10_000;
        pointRepository.save(new Point(user, initialBalance));

        PaymentRequest request = new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT);

        // when & then
        assertThatThrownBy(() -> paymentFacade.pay(user, request))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }

    /**
     * 30명이 동시에 상품 구매를 시도하고, 상품 재고가 떨어진 경우 예외 발생을 검증하는 테스트입니다.
     * 각 유저는 1개씩 구매, 상품 재고는 20개로 가정합니다. (성공: 20명, 실패: 10명)
     */
    @DisplayName("재고가 20개인 상품을 30명의 서로 다른 유저가 한 개씩 주문 할 때, 10명은 해당 상품 주문 건 결제에 실패한다.")
    @Test
    void payConcurrently_FailureAfterExceededStockQuantity() throws InterruptedException {
        // given
        Category category = categoryRepository.save(CategoryFixture.create("상의"));
        Product product = productRepository.save(new Product("라넌큘러스 오버핏 맨투맨", category, 12_000, 20));

        int threads = 30;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<User> users = new ArrayList<>();
        List<PaymentRequest> requests = new ArrayList<>();
        for (int i = 1; i <= threads; i++) {
            User user = userRepository.save(UserFixture.USER());
            pointRepository.save(new Point(user, 100_000));
            users.add(user);

            Order order = new Order(user);
            order.addOrderProduct(product, 1);
            orderRepository.save(order);

            requests.add(new PaymentRequest(order.getId(), PaymentMethod.POINT_PAYMENT));
        }

        // when
        executeConcurrency(threads, idx -> {
            try {
                paymentFacade.pay(users.get(idx), requests.get(idx));
                successCount.incrementAndGet();
            } catch (InsufficientStockException e) {
                failCount.incrementAndGet();
            }
        });

        // then
        assertThat(successCount.get()).isEqualTo(20);
        assertThat(failCount.get()).isEqualTo(10);

        Product foundProduct = productRepository.findById(product.getId());
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