package kr.hhplus.be.server.api.coupon.controller;

import kr.hhplus.be.server.api.coupon.controller.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.controller.response.CouponResponse;
import kr.hhplus.be.server.api.coupon.facade.CouponFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponFacade couponFacade;

    @GetMapping
    public ResponseEntity<List<CouponResponse>> findAvailableCoupons(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        List<CouponResponse> response = couponFacade.findAvailableCoupons(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CouponResponse> issue(@RequestBody CouponIssueRequest request) {
        CouponResponse response = couponFacade.issue(request);
        return ResponseEntity.ok(response);
    }
}
