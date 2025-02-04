package kr.hhplus.be.server.domain.support.kafka;

public interface DataPlatformClient {

    void send(PaymentCompletedEvent event);
}
