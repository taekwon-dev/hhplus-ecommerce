package kr.hhplus.be.server.domain.payment.domain;

public interface PaymentStrategy {

    void pay(long userId, int amount);
}
