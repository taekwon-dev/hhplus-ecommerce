package kr.hhplus.be.server.infra.kafka;

import kr.hhplus.be.server.domain.support.kafka.DataPlatformClient;
import kr.hhplus.be.server.domain.support.kafka.PaymentCompletedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(PaymentCompletedEvent event) {
    }
}
