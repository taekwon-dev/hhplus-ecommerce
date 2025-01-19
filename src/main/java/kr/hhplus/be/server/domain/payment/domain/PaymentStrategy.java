package kr.hhplus.be.server.domain.payment.domain;

import kr.hhplus.be.server.domain.user.domain.User;

public interface PaymentStrategy {

    void pay(User user, int amount);
}
