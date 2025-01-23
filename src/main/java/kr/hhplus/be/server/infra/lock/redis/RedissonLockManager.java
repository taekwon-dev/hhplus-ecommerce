package kr.hhplus.be.server.infra.lock.redis;

import kr.hhplus.be.server.infra.lock.LockManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonLockManager implements LockManager {

    private final RedissonClient redissonClient;

    @Override
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock rLock = redissonClient.getLock(key);
        return rLock.tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public void unlock(String key) {
        RLock rLock = redissonClient.getLock(key);
        rLock.unlock();
    }
}
