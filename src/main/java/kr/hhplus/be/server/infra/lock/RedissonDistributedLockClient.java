package kr.hhplus.be.server.infra.lock;

import kr.hhplus.be.server.domain.support.lock.DistributedLockManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonDistributedLockClient implements DistributedLockManager {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock rLock = getLock(key);
        return rLock.tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public void unlock(String key) {
        RLock rLock = getLock(key);
        rLock.unlock();
    }

    private RLock getLock(String key) {
        return redissonClient.getLock(key);
    }
}
