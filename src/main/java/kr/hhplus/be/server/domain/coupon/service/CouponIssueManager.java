package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.service.dto.CouponIssueParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CouponIssueManager {

    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon_issue_queue";
    private static final String ISSUED_COUPONS_KEY = "issued_coupons";

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(CouponIssueParam param) {
        redisTemplate.opsForZSet().add(COUPON_ISSUE_QUEUE_KEY, param, Instant.now().toEpochMilli());
    }

    public List<CouponIssueParam> consume(int batch) {
        Set<Object> requests = redisTemplate.opsForZSet().range(COUPON_ISSUE_QUEUE_KEY, 0, batch - 1);
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(request -> (CouponIssueParam) request)
                .collect(Collectors.toList());
    }

    public boolean validateAlreadyIssued(CouponIssueParam param) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ISSUED_COUPONS_KEY, param));
    }

    public void markCouponAsIssued(CouponIssueParam param) {
        redisTemplate.opsForSet().add(ISSUED_COUPONS_KEY, param);
    }

    public void removeCouponRequest(CouponIssueParam param) {
        redisTemplate.opsForZSet().remove(COUPON_ISSUE_QUEUE_KEY, param);
    }
}
