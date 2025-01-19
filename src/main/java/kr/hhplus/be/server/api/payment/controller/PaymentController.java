package kr.hhplus.be.server.api.payment.controller;

import kr.hhplus.be.server.api.payment.controller.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.response.PaymentResponse;
import kr.hhplus.be.server.domain.payment.facade.PaymentFacade;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestBody PaymentRequest request,
            User user
    ) {
        PaymentResponse response = paymentFacade.pay(user, request);
        return ResponseEntity.ok(response);
    }
}
