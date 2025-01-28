package kr.hhplus.be.server.integration.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.exception.AlreadyIssuedCouponException;
import kr.hhplus.be.server.domain.coupon.exception.MaxIssuableCountExceededException;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
class CouponServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    @DisplayName("유저가 사용 가능한 쿠폰 목록을 조회한다.")
    @Test
    void findAvailableCouponsByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user, coupon.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Coupon> coupons = couponService.findAvailableCoupons(user, pageable);

        // then
        assertThat(coupons).hasSize(1);
    }

    @DisplayName("유저에게 쿠폰을 발급한다.")
    @Test
    void issue() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));

        // when
        Coupon issuedCoupon = couponService.issue(user, coupon.getId());

        // then
        assertThat(issuedCoupon.getIssuedCount()).isEqualTo(1);
    }

    @DisplayName("쿠폰 발급 시, 발급 가능 수량을 초과한 경우 예외가 발생한다.")
    @Test
    void issue_exceededMaxIssuableCount() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 0));

        // when & then
        assertThatThrownBy(() -> couponService.issue(user, coupon.getId()))
                .isInstanceOf(MaxIssuableCountExceededException.class);
    }

    @DisplayName("쿠폰 발급 시, 이미 유저에게 쿠폰 발급 이력이 있는 경우 예외가 발생한다.")
    @Test
    void issue_alreadyIssuedCoupon() {
        // given
        User user = userRepository.save(UserFixture.USER());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));
        couponService.issue(user, coupon.getId());

        // when & then
        assertThatThrownBy(() -> couponService.issue(user, coupon.getId()))
                .isInstanceOf(AlreadyIssuedCouponException.class);
    }

    /**
     * 20명이 동시에 쿠폰 발급을 시도하고, 모든 쿠폰이 발급된 경우 예외 발생을 검증하는 테스트입니다. (성공: 10명, 실패: 10명)
     */
    @DisplayName("최대 발급 수량이 10개인 쿠폰을 20명의 서로 다른 유저가 쿠폰 발급을 신청할 때, 10개의 쿠폰이 모두 발급된 경우 나머지 10명은 쿠폰 발급에 실패한다.")
    @Test
    void issueConcurrently_exceededMaxIssuableCount() throws InterruptedException {
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
                couponService.issue(users.get(idx), coupon.getId());
                successCount.incrementAndGet();
            } catch (MaxIssuableCountExceededException e) {
                failCount.incrementAndGet();
            }
        });

        // then
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(10);
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
