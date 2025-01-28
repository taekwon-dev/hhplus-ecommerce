package kr.hhplus.be.server.api.payment.controller;

import kr.hhplus.be.server.api.payment.application.dto.response.PaymentResult;
import kr.hhplus.be.server.api.payment.controller.dto.request.PaymentRequest;
import kr.hhplus.be.server.api.payment.controller.dto.response.PaymentResponse;
import kr.hhplus.be.server.api.payment.application.PaymentFacade;
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
    public ResponseEntity<PaymentResponse> pay(User user, @RequestBody PaymentRequest request) {
        PaymentResult result = paymentFacade.pay(user.getId(), request.toPaymentParam());
        return ResponseEntity.ok(PaymentResponse.from(result));
    }
}
