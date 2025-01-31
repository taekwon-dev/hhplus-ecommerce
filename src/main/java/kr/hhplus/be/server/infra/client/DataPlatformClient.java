package kr.hhplus.be.server.infra.client;

public interface DataPlatformClient {

    void send(PaymentCompletedEvent event);
}
