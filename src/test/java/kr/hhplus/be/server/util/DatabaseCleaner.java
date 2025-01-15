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

    private static final String CAMEL_CASE = "([a-z])([A-Z])";
    private static final String SNAKE_CASE = "$1_$2";

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @PostConstruct
    public void afterPropertiesSet() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        tableNames = new ArrayList<>(entities.stream()
                .filter(entity -> entity.getJavaType().isAnnotationPresent(Entity.class))
                .map(entity -> {
                    if (entity.getJavaType().isAnnotationPresent(Table.class)) {
                        return entity.getJavaType().getAnnotation(Table.class).name();
                    } else {
                        return convertCamelToSnake(entity);
                    }
                })
                .toList());
    }

    @Transactional
    public void execute() {
        entityManager.clear();
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

    private String convertCamelToSnake(final EntityType<?> e) {
        return e.getName()
                .replaceAll(CAMEL_CASE, SNAKE_CASE)
                .toLowerCase();
    }
}
