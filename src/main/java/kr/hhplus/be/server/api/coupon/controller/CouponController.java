package kr.hhplus.be.server.api.coupon.controller;

import kr.hhplus.be.server.api.coupon.application.dto.AvailableCouponsResult;
import kr.hhplus.be.server.api.coupon.controller.dto.request.IssueCouponRequest;
import kr.hhplus.be.server.api.coupon.controller.dto.response.AvailableCouponsResponse;
import kr.hhplus.be.server.api.coupon.application.CouponFacade;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponFacade couponFacade;

    @GetMapping
    public ResponseEntity<AvailableCouponsResponse> findAvailableCoupons(User user, Pageable pageable) {
        AvailableCouponsResult result = couponFacade.findAvailableCoupons(user.getId(), pageable);
        return ResponseEntity.ok(AvailableCouponsResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<Void> issueCoupon(User user, @RequestBody IssueCouponRequest request) {
        couponFacade.sendCouponIssueRequest(user.getId(), request.couponId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
