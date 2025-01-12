package kr.hhplus.be.server.util;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@ActiveProfiles("test")
public class DatabaseCleaner {

    private static final String TRUNCATE_FORMAT = "TRUNCATE TABLE %s";
    private static final String ALTER_FORMAT = "ALTER TABLE %s ALTER COLUMN ID RESTART WITH 1";

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @PostConstruct
    public void afterPropertiesSet() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        tableNames = new ArrayList<>(entities.stream()
                .filter(entity -> entity.getJavaType().isAnnotationPresent(Table.class))
                .map(entity -> entity.getJavaType().getAnnotation(Table.class).name())
                .toList());
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery("SET foreign_key_checks = 0;").executeUpdate();
        tableNames.forEach(tableName -> executeQueryWithTable(tableName));
        entityManager.createNativeQuery("SET foreign_key_checks = 1;").executeUpdate();
    }

    private void executeQueryWithTable(String tableName) {
        entityManager.createNativeQuery("TRUNCATE TABLE " + tableName + ";").executeUpdate();
        entityManager
                .createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1;")
                .executeUpdate();
    }

}
