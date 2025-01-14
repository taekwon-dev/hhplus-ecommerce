package kr.hhplus.be.server.domain.payment.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final PointPaymentStrategy pointPaymentStrategy;

    public PaymentStrategy getPaymentStrategy(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case POINT_PAYMENT -> pointPaymentStrategy;
        };
    }
}
