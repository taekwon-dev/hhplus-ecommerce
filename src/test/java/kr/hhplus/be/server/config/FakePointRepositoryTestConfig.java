package kr.hhplus.be.server.config;

import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.integration.domain.point.repository.fake.FakePointCoreRepository;
import kr.hhplus.be.server.integration.domain.point.repository.fake.FakePointJpaRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakePointRepositoryTestConfig {

    @Bean
    public PointRepository pointRepository(FakePointJpaRepository fakePointJpaRepository) {
        return new FakePointCoreRepository(fakePointJpaRepository);
    }
}
