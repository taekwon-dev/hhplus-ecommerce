package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.service.dto.CouponIssueParam;
import kr.hhplus.be.server.util.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("쿠폰 발급 Manager 통합 테스트")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CouponIssueManagerIntegrationTest {

    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon_issue_queue";
    private static final String ISSUED_COUPONS_KEY = "issued_coupons";

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

    @DisplayName("쿠폰 발급 요청을 Redis ZSET 자료 구조에 추가한다.")
    @Test
    void publish() {
        // given
        CouponIssueParam param = new CouponIssueParam(1L, 1L);

        // when
        couponIssueManager.publish(param);

        // then
        assertThat(redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY)).isOne();
        assertThat(redisTemplate.opsForZSet().range(COUPON_ISSUE_QUEUE_KEY, 0, -1)).contains(param);
    }

    @DisplayName("Redis ZSET 자료 구조에서 쿠폰 발급 요청 목록을 배치 단위로 읽어온다.")
    @Test
    void consume() {
        // given
        int batch = 3;
        CouponIssueParam param1 = new CouponIssueParam(1L, 1L);
        CouponIssueParam param2 = new CouponIssueParam(2L, 1L);
        CouponIssueParam param3 = new CouponIssueParam(3L, 1L);

        couponIssueManager.publish(param1);
        couponIssueManager.publish(param2);
        couponIssueManager.publish(param3);

        // when
        List<CouponIssueParam> params = couponIssueManager.consume(batch);

        // then
        assertThat(params.size()).isEqualTo(batch);
        assertThat(redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY)).isEqualTo(batch);
    }

    @DisplayName("쿠폰 발급 요청이 처리된 경우, Redis SET 자료 구조에서 발급 처리 여부를 확인 할 수 있다.")
    @Test
    void validateAlreadyIssuedCoupon() {
        // given
        CouponIssueParam param = new CouponIssueParam(1L, 1L);
        couponIssueManager.markCouponAsIssued(param);

        // when
        boolean result = couponIssueManager.validateAlreadyIssued(param);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("쿠폰 발급 성공 시, Redis SET 자료 구조에 발급 기록을 저장한다.")
    @Test
    void markCouponAsIssued() {
        // given
        CouponIssueParam param = new CouponIssueParam(1L, 1L);

        // when
        couponIssueManager.markCouponAsIssued(param);

        // then
        assertThat(redisTemplate.opsForSet().size(ISSUED_COUPONS_KEY)).isOne();
        assertThat(redisTemplate.opsForSet().isMember(ISSUED_COUPONS_KEY, param)).isTrue();
    }

    @DisplayName("쿠폰 발급 성공 시, Redis ZSET 자료 구조에서 쿠폰 발급 요청을 제거한다.")
    @Test
    void removeCouponRequest() {
        // given
        CouponIssueParam param = new CouponIssueParam(1L, 1L);
        couponIssueManager.publish(param);

        // when
        couponIssueManager.removeCouponRequest(param);

        // then
        assertThat(redisTemplate.opsForZSet().size(COUPON_ISSUE_QUEUE_KEY)).isZero();
        assertThat(redisTemplate.opsForZSet().range(COUPON_ISSUE_QUEUE_KEY, 0, -1)).doesNotContain(param);
    }
}