package kr.hhplus.be.server.domain.support.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLockManager {

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException; // InterruptedException 필수인가? (인터페이스에 정의하면 이를 구현한 애들도 얘를 던질 수 있음)

    void unlock(String key);
}
