package kr.hhplus.be.server.integration.domain.point.service;

import kr.hhplus.be.server.domain.point.domain.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointDeductionAmountException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PointServiceTest {

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

    @DisplayName("유저의 포인트 잔액 조회 시, 포인트 잔액을 조회할 수 없는 경우 0으로 초기화 한 뒤 반환한다.")
    @Test
    void findPointByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        // when
        Point point = pointService.findPointByUser(user);

        // then
        assertThat(point.getId()).isNull();
        assertThat(point.getUser()).isEqualTo(user);
        assertThat(point.getBalance()).isZero();
    }

    @DisplayName("유저의 포인트 잔액을 조회한다.")
    @Test
    void findPointByUser_AlreadyExist() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user, initialBalance));

        // when
        Point point = pointService.findPointByUser(user);

        // then
        assertThat(point.getId()).isNotNull();
        assertThat(point.getUser().getId()).isEqualTo(user.getId());
        assertThat(point.getBalance()).isEqualTo(initialBalance);
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void addPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user, initialBalance));
        int amountToCharge = 2_000;

        // when
        Point chargedPoint = pointService.addPoints(user, amountToCharge);

        // then
        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chargedPoint.getBalance()).isEqualTo(initialBalance + amountToCharge);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 0 또는 음수인 경우 예외가 발생한다.")
    @Test
    void addPoints_Fail_InvalidMinimumAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 0;

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(user, amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 1,000 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void addPoints_Fail_InvalidAmountUnit() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 1_500;

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(user, amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void deductPoints() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user, initialBalance));
        int amountToDeduct = 1_000;

        // when
        Point chargedPoint = pointService.deductPoints(user, amountToDeduct);

        // then
        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chargedPoint.getBalance()).isZero();
    }

    @DisplayName("포인트 사용 시, 요청 포인트가 0보다 작은 경우 예외가 발생한다.")
    @Test
    void deductPoints_Fail_InvalidAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 0;

        // when & then
        assertThatThrownBy(() -> pointService.deductPoints(user, amountToDeduct))
                .isInstanceOf(InvalidPointDeductionAmountException.class);
    }

    @DisplayName("포인트 사용 시, 포인트 잔액보다 요청 포인트가 큰 경우 예외가 발생한다.")
    @Test
    void deductPoints_Fail_InsufficientBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 2_000;
        int initialBalance = 1_000;
        pointRepository.save(new Point(user, initialBalance));

        // when & then
        assertThatThrownBy(() -> pointService.deductPoints(user, amountToDeduct))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 충전을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다. (최초 잔액: 1,000원, 최종 잔액: 10,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 충전을 요청한다.")
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
        assertThat(chagedPoint.getBalance()).isEqualTo(initialBalance + (amountToCharge * threads));
        assertThat(successCount.get()).isEqualTo(threads);
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 사용을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다. (최초 잔액: 10,000원, 최종 잔액: 1,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 사용을 요청한다.")
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
        assertThat(deductedPoint.getBalance()).isEqualTo(initialBalance - (amountToDeduct * threads));
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

    /**
     * 동일한 유저가 동시에 포인트 충전과 사용을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다. (최초 잔액: 10,000원, 최종 잔액: 10,000원)
     */
    @DisplayName("동일한 유저가 동시에 포인트 충전과 사용을 요청한다.")
    @Test
    void addAndDeductPointsConcurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 5_000;
        int amountToDeduct = 5_000;
        int initialBalance = 10_000;
        Point point = pointRepository.save(new Point(user, initialBalance));

        int threads = 1;
        AtomicInteger successCount = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            pointService.addPoints(user, amountToCharge);
            successCount.incrementAndGet();
        });

        tasks.add(() -> {
            pointService.deductPoints(user, amountToDeduct);
            successCount.incrementAndGet();
        });

        // when
        executeConcurrency(threads, tasks);

        // then
        Point finalPoint = pointRepository.findByUser(user);

        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(finalPoint.getBalance()).isEqualTo(initialBalance + (amountToCharge - amountToDeduct));
        assertThat(successCount.get()).isEqualTo(2);
    }

    private void executeConcurrency(int threads, List<Runnable> tasks) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(tasks.size());
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        for (Runnable task : tasks) {
            for (int i = 0; i < threads; i++) {
                executor.execute(() -> {
                    try {
                        task.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        latch.await();
        executor.shutdown();
    }
}
