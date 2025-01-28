package kr.hhplus.be.server.infra.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public interface LockManager {

    RLock getLock(String key);

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    void unlock(String key);
}
