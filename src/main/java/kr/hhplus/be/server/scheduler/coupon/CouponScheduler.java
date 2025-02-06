package kr.hhplus.be.server.scheduler.coupon;

import kr.hhplus.be.server.domain.coupon.service.CouponIssueManager;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.service.dto.CouponIssueParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponService couponService;
    private final CouponIssueManager couponIssueManager;

    @Value("${schedules.batch.coupon-issue-size}")
    private int couponIssueBatchSize;

    @Scheduled(cron = "${schedules.cron.coupon.issue}")
    public void issueCouponScheduler() {
        List<CouponIssueParam> params = couponIssueManager.consume(couponIssueBatchSize);
        for (CouponIssueParam param : params) {
            if (!couponIssueManager.validateAlreadyIssued(param)) {
                couponService.issue(param.userId(), param.couponId());
                couponIssueManager.markCouponAsIssued(param);
            }
            couponIssueManager.removeCouponRequest(param);
        }
    }
}
