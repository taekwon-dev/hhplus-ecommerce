package kr.hhplus.be.server.api.coupon.facade;

import kr.hhplus.be.server.api.coupon.controller.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.controller.response.CouponResponse;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponFacade {

    private final CouponService couponService;

    @Transactional(readOnly = true)
    public List<CouponResponse> findAvailableCoupons(User user, Pageable pageable) {
        List<Coupon> coupons = couponService.findAvailableCoupons(user, pageable);
        return mapToCouponResponses(coupons);
    }

    private List<CouponResponse> mapToCouponResponses(List<Coupon> coupons) {
        return coupons.stream()
                .map(coupon -> new CouponResponse(coupon.getId(), coupon.getCode(), coupon.getStartDate(), coupon.getEndDate()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponResponse issue(User user, CouponIssueRequest request) {
        Coupon coupon = couponService.issue(user, request.couponId());
        return new CouponResponse(coupon.getId(), coupon.getCode(), coupon.getStartDate(), coupon.getEndDate());
    }
}
