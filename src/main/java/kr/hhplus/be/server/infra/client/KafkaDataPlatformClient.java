package kr.hhplus.be.server.infra.client;

import org.springframework.stereotype.Component;

@Component
public class KafkaDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(PaymentCompletedEvent event) {
    }
}
