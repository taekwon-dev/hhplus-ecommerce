package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.Point;
import kr.hhplus.be.server.domain.point.exception.InsufficientPointBalanceException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointAdditionAmountException;
import kr.hhplus.be.server.domain.point.exception.InvalidPointDeductionAmountException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.util.ServiceTest;
import kr.hhplus.be.server.util.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 Service 통합 테스트")
class PointServiceIntegrationTest extends ServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @DisplayName("유저의 포인트 잔액 조회 시, 포인트 잔액을 조회할 수 없는 경우 0으로 초기화 한 뒤 반환한다.")
    @Test
    void findPointByUser() {
        // given
        User user = userRepository.save(UserFixture.USER());

        // when
        Point point = pointService.findPointByUserId(user.getId());

        // then
        assertThat(point.getId()).isNull();
        assertThat(point.getUserId()).isEqualTo(user.getId());
        assertThat(point.getBalance()).isZero();
    }

    @DisplayName("유저의 포인트 잔액을 조회한다.")
    @Test
    void findPointByUser_AlreadyExist() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when
        Point point = pointService.findPointByUserId(user.getId());

        // then
        assertThat(point.getId()).isNotNull();
        assertThat(point.getUserId()).isEqualTo(user.getId());
        assertThat(point.getBalance()).isEqualTo(initialBalance);
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void charge() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user.getId(), initialBalance));
        int amountToCharge = 2_000;

        // when
        Point chargedPoint = pointService.charge(user.getId(), amountToCharge);

        // then
        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chargedPoint.getBalance()).isEqualTo(initialBalance + amountToCharge);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 0 또는 음수인 경우 예외가 발생한다.")
    @Test
    void charge_invalidMinimumAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 0;

        // when & then
        assertThatThrownBy(() -> pointService.charge(user.getId(), amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트 충전 시, 요청 포인트가 1,000 단위가 아닌 경우 예외가 발생한다.")
    @Test
    void charge_invalidAmountUnit() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 1_500;

        // when & then
        assertThatThrownBy(() -> pointService.charge(user.getId(), amountToCharge))
                .isInstanceOf(InvalidPointAdditionAmountException.class);
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void deduct() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user.getId(), initialBalance));
        int amountToDeduct = 1_000;

        // when
        Point chargedPoint = pointService.deduct(user.getId(), amountToDeduct);

        // then
        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chargedPoint.getBalance()).isZero();
    }

    @DisplayName("포인트 사용 시, 요청 포인트가 0보다 작은 경우 예외가 발생한다.")
    @Test
    void deduct_invalidAmount() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 0;

        // when & then
        assertThatThrownBy(() -> pointService.deduct(user.getId(), amountToDeduct))
                .isInstanceOf(InvalidPointDeductionAmountException.class);
    }

    @DisplayName("포인트 사용 시, 포인트 잔액보다 요청 포인트가 큰 경우 예외가 발생한다.")
    @Test
    void deduct_insufficientBalance() {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 2_000;
        int initialBalance = 1_000;
        pointRepository.save(new Point(user.getId(), initialBalance));

        // when & then
        assertThatThrownBy(() -> pointService.deduct(user.getId(), amountToDeduct))
                .isInstanceOf(InsufficientPointBalanceException.class);
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 충전을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다. (최초 잔액: 1,000원, 최종 잔액: 10,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 충전을 요청한다.")
    @Test
    void charge_concurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 3_000;
        int initialBalance = 1_000;
        Point point = pointRepository.save(new Point(user.getId(), initialBalance));

        int threads = 3;
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        executeConcurrency(threads, () -> {
            pointService.charge(user.getId(), amountToCharge);
            successCount.incrementAndGet();
        });

        // then
        Point chagedPoint = pointRepository.findByUserId(user.getId());

        assertThat(point.getBalance()).isEqualTo(initialBalance);
        assertThat(chagedPoint.getBalance()).isEqualTo(initialBalance + (amountToCharge * threads));
        assertThat(successCount.get()).isEqualTo(threads);
    }

    /**
     * 동일한 유저가 동시에 3번 포인트 사용을 시도하고, 각 요청이 순차적으로 처리되어 잔액이 정확한지 확인하는 테스트입니다. (최초 잔액: 10,000원, 최종 잔액: 1,000원)
     */
    @DisplayName("동일한 유저가 동시에 3번 포인트 사용을 요청한다.")
    @Test
    void deduct_concurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToDeduct = 3_000;
        int initialBalance = 10_000;
        Point point = pointRepository.save(new Point(user.getId(), initialBalance));

        int threads = 3;
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        executeConcurrency(threads, () -> {
            pointService.deduct(user.getId(), amountToDeduct);
            successCount.incrementAndGet();
        });

        // then
        Point deductedPoint = pointRepository.findByUserId(user.getId());

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
    void chargeAndDeduct_concurrently() throws InterruptedException {
        // given
        User user = userRepository.save(UserFixture.USER());
        int amountToCharge = 5_000;
        int amountToDeduct = 5_000;
        int initialBalance = 10_000;
        Point point = pointRepository.save(new Point(user.getId(), initialBalance));

        int threads = 1;
        AtomicInteger successCount = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            pointService.charge(user.getId(), amountToCharge);
            successCount.incrementAndGet();
        });

        tasks.add(() -> {
            pointService.deduct(user.getId(), amountToDeduct);
            successCount.incrementAndGet();
        });

        // when
        executeConcurrency(threads, tasks);

        // then
        Point finalPoint = pointRepository.findByUserId(user.getId());

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
