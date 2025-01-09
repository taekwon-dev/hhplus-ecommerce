package kr.hhplus.be.server.integration.domain.point.service;

import kr.hhplus.be.server.config.FakePointRepositoryTestConfig;
import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.DatabaseCleaner;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = FakePointRepositoryTestConfig.class)
class FakePointServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 충전을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다.
     * 동시성 제어 적용되지 않은 Fake Repo 사용으로 실제로 오류가 발생하는지 검증합니다. (최초 잔액: 1,000원, 기대 최종 잔액: 10,000원, 실제 최종 잔액: 4,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 충전을 시도한다.")
    @Test
    void addPointsConcurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 3_000;
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user, initialBalance));

        int threads = 3;
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        executeConcurrency(threads, () -> {
            pointService.addPoints(user, amountToCharge);
            successCount.incrementAndGet();
        });

        // then
        Point chagedPoint = pointRepository.findByUser(user);

        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chagedPoint.getBalance()).isEqualTo(initialBalance + (amountToCharge * 1));
        assertThat(successCount.get()).isEqualTo(threads);
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 이용을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다.
     * 동시성 제어 적용되지 않은 Fake Repo 사용으로 실제로 오류가 발생하는지 검증합니다. (최초 잔액: 10,000원, 기대 최종 잔액: 1,000원, 실제 최종 잔액: 7,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 이용을 시도한다.")
    @Test
    void deductPointsConcurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 3_000;
        int initialBalance = 10_000;
        Point point = pointRepository.save(new Point(user, initialBalance));

        int threads = 3;
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        executeConcurrency(threads, () -> {
            pointService.deductPoints(user, amountToDeduct);
            successCount.incrementAndGet();
        });

        // then
        Point deductedPoint = pointRepository.findByUser(user);

        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(deductedPoint.getBalance()).isEqualTo(initialBalance - (amountToDeduct * 1));
        assertThat(successCount.get()).isEqualTo(threads);
    }

    private void executeConcurrency(int threads, Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                task.run();
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();
    }
}
