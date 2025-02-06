package kr.hhplus.be.server.scheduler.coupon;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponDiscountType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.IssuedCouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponIssueManager;
import kr.hhplus.be.server.domain.coupon.service.dto.CouponIssueParam;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.CouponFixture;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("쿠폰 발급 Scheduler 통합 테스트")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CouponSchedulerIntegrationTest {

    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon_issue_queue";
    private static final String ISSUED_COUPONS_KEY = "issued_coupons";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Autowired
    private CouponIssueManager couponIssueManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
    }

    @DisplayName("쿠폰 발급 요청이 있는 경우, 쿠폰 발급 스케줄러가 쿠폰 발급을 처리한다.")
    @Test
    void issueCouponScheduler() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));

        CouponIssueParam param = new CouponIssueParam(user.getId(), coupon.getId());
        couponIssueManager.publish(param);

        Awaitility
                .await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> issuedCouponRepository.existsByUserIdAndCoupon(user.getId(), coupon), equalTo(true));

        assertThat(redisTemplate.opsForSet().isMember(ISSUED_COUPONS_KEY, param)).isTrue();
        assertThat(redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY)).isZero();
    }

    @DisplayName("이미 발급된 이력이 있는 경우, 쿠폰 발급 스케줄러가 쿠폰 발급 처리를 하지 않는다.")
    @Test
    void issueCouponScheduler_withAlreadyIssuedCoupon() {
        // given
        User user = userRepository.save(UserFixture.USER());
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusWeeks(1);
        Coupon coupon = couponRepository.save(CouponFixture.create(CouponDiscountType.RATE, 10, startDate, endDate, 10));

        CouponIssueParam param = new CouponIssueParam(user.getId(), coupon.getId());
        couponIssueManager.publish(param);

        Awaitility
                .await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> issuedCouponRepository.existsByUserIdAndCoupon(user.getId(), coupon), equalTo(true));

        assertThat(redisTemplate.opsForSet().isMember(ISSUED_COUPONS_KEY, param)).isTrue();
        assertThat(redisTemplate.opsForSet().size(ISSUED_COUPONS_KEY)).isOne();
        assertThat(redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY)).isZero();

        couponIssueManager.publish(param);

        assertThatCode(() -> Awaitility
                .await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY), equalTo(0L)))
                .doesNotThrowAnyException();

        assertThat(redisTemplate.opsForSet().isMember(ISSUED_COUPONS_KEY, param)).isTrue();
        assertThat(redisTemplate.opsForSet().size(ISSUED_COUPONS_KEY)).isOne();
    }
}