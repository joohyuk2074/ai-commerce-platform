package com.spartaecommerce.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spartaecommerce.common.infrastructure.persistence.jpa.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * JPA 테스트용 추상 베이스 클래스
 * - JPA 전용 빈만 로드하여 테스트 실행 속도 향상
 * - QueryDSL 설정을 포함하여 동적 쿼리 테스트 지원
 * - 컨텍스트 캐싱을 통해 테스트 간 성능 최적화
 * - H2 인메모리 데이터베이스 사용
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(QueryDslConfig.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
public abstract class JpaTestSupport {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected JPAQueryFactory queryFactory;

    /**
     * 각 테스트 후 영속성 컨텍스트 초기화
     * - 테스트 간 데이터 격리 보장
     * - 1차 캐시 초기화로 정확한 쿼리 테스트
     */
    @AfterEach
    void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * 테스트 데이터를 즉시 영속화하고 초기화
     * - INSERT 쿼리 즉시 실행
     * - 영속성 컨텍스트 초기화로 조회 쿼리 테스트 보장
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * 엔티티를 영속화하고 즉시 DB에 반영
     */
    protected <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
}
