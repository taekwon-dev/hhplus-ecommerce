package kr.hhplus.be.server.domain.payment.infra;

import kr.hhplus.be.server.domain.payment.domain.DataPlatformClient;
import kr.hhplus.be.server.domain.payment.domain.PaymentCompletedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(PaymentCompletedEvent event) {
    }
}
