package kr.hhplus.be.server.api.coupon.application;

import kr.hhplus.be.server.api.coupon.application.dto.AvailableCouponsResult;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infra.storage.core.jpa.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("쿠폰 Facade 통합 테스트")
class CouponFacadeTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponFacade couponFacade;

    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user.getId(), coupon.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        AvailableCouponsResult result = couponFacade.findAvailableCoupons(user.getId(), pageable);

        // then
        assertThat(result.coupons()).hasSize(1);
    }

    /**
     * 20명이 동시에 쿠폰 발급을 시도하고, 모든 쿠폰이 발급된 경우 예외 발생을 검증하는 테스트입니다. (성공: 10명, 실패: 10명)
     */
    @DisplayName("최대 발급 수량이 10개인 쿠폰을 20명의 서로 다른 유저가 쿠폰 발급을 신청할 때, 10개의 쿠폰이 모두 발급된 경우 나머지 10명은 쿠폰 발급에 실패한다.")
    @Test
    void sendCouponIssueRequestConcurrently_exceededMaxIssuableCount() throws InterruptedException {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));

        int threads = 20;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            users.add(userRepository.save(UserFixture.USER()));
        }

        // when
        executeConcurrency(threads, idx -> {
            try {
                couponFacade.sendCouponIssueRequest(users.get(idx).getId(), coupon.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        });

        // then
        assertThat(successCount.get()).isEqualTo(20);
        assertThat(failCount.get()).isZero();

        Awaitility
                .await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(issuedCouponJpaRepository.count()).isEqualTo(10));
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
