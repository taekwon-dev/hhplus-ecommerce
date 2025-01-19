package kr.hhplus.be.server.domain.payment.domain;

public interface DataPlatformClient {

    void send(PaymentCompletedEvent event);
}
