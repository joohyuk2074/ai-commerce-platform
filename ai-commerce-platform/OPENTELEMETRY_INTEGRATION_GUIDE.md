# OpenTelemetry 분산 추적 통합 가이드

## 목차
1. [개요](#개요)
2. [아키텍처](#아키텍처)
3. [방법 1: Java Agent (권장)](#방법-1-java-agent-권장)
4. [방법 2: Spring Boot Dependency](#방법-2-spring-boot-dependency)
5. [로그와 트레이스 연동](#로그와-트레이스-연동)
6. [Kafka 이벤트 추적](#kafka-이벤트-추적)
7. [트러블슈팅](#트러블슈팅)

---

## 개요

### 분산 추적이란?
마이크로서비스 아키텍처에서 하나의 사용자 요청이 여러 서비스를 거쳐 처리됩니다. 분산 추적은 이러한 요청의 전체 흐름을 추적하고 시각화하여 성능 병목과 에러를 빠르게 파악할 수 있게 해줍니다.

### 핵심 개념
- **Trace**: 하나의 완전한 요청 흐름 (예: 주문 생성 → 재고 확인 → 결제 → 포인트 적립)
- **Span**: 하나의 작업 단위 (예: DB 쿼리, HTTP 호출, 메서드 실행)
- **Context Propagation**: 서비스 간 trace ID를 전달하여 연결

---

## 아키텍처

```
┌─────────────────┐
│  사용자 요청     │
└────────┬────────┘
         │
    ┌────▼──────────────────────────────────────┐
    │  Spring Boot Services                     │
    │  - auth-service                            │
    │  - ecommerce-service                       │
    │  - product-service                         │
    │  - order-service                           │
    │                                            │
    │  [OpenTelemetry Java Agent]                │
    │  자동으로 트레이스 생성 및 전송             │
    └────┬───────────────────────────────────────┘
         │ OTLP (gRPC/HTTP)
         │
    ┌────▼──────────────────────┐
    │  OpenTelemetry Collector  │
    │  - 데이터 수집/처리/라우팅  │
    │  - 배치 처리 최적화         │
    └────┬──────────────────────┘
         │
    ┌────▼──────────┐
    │    Jaeger     │
    │  - 트레이스 저장│
    │  - UI 제공     │
    │  - ES 통합     │
    └───────────────┘
         │
    ┌────▼──────────┐
    │ Elasticsearch │
    │  - 트레이스 저장│
    │  - 로그 저장   │
    └───────────────┘
```

---

## 방법 1: Java Agent (권장)

### 특징
- ✅ **Zero Code Change**: 코드 수정 없이 적용
- ✅ **자동 계측**: HTTP, JDBC, Kafka, Redis 등 자동 추적
- ✅ **빠른 적용**: 설정 파일만으로 완료
- ❌ 커스텀 Span 추가가 제한적 (비즈니스 로직 추적 어려움)

### 1단계: Java Agent 다운로드

```bash
cd /Users/joohyuk/Documents/study/project/project2/ai-commerce-platform

# OpenTelemetry Java Agent 다운로드 (최신 버전)
curl -L -o opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

### 2단계: 각 서비스 실행 시 Agent 적용

#### 로컬 개발 환경

**IntelliJ IDEA / Eclipse 설정:**

1. Run Configuration 열기
2. VM Options에 다음 추가:

```
-javaagent:/Users/joohyuk/Documents/study/project/project2/ai-commerce-platform/opentelemetry-javaagent.jar
-Dotel.service.name=ecommerce-service
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.metrics.exporter=none
-Dotel.logs.exporter=none
```

**서비스별 이름:**
- `auth-service`: 인증/인가 서비스
- `ecommerce-service`: 이커머스 코어 서비스
- `product-service`: 상품 관리 서비스
- `order-service`: 주문 처리 서비스

#### Gradle 실행

`build.gradle`에 추가:

```gradle
tasks.named('bootRun') {
    jvmArgs = [
        "-javaagent:${rootProject.projectDir}/opentelemetry-javaagent.jar",
        "-Dotel.service.name=ecommerce-service",
        "-Dotel.exporter.otlp.endpoint=http://localhost:4317",
        "-Dotel.metrics.exporter=none",
        "-Dotel.logs.exporter=none"
    ]
}
```

실행:
```bash
./gradlew :ecommerce-service:bootRun
```

#### Docker Compose 환경

`docker-compose.yml`에 서비스 추가:

```yaml
ecommerce-service:
  build:
    context: ./ecommerce-service
    dockerfile: Dockerfile
  container_name: sparta-ecommerce-app
  ports:
    - "8082:8082"
  environment:
    # OpenTelemetry 설정
    - JAVA_TOOL_OPTIONS=-javaagent:/app/opentelemetry-javaagent.jar
    - OTEL_SERVICE_NAME=ecommerce-service
    - OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
    - OTEL_METRICS_EXPORTER=none
    - OTEL_LOGS_EXPORTER=none

    # 추가 설정
    - OTEL_TRACES_SAMPLER=always_on
    - OTEL_PROPAGATORS=tracecontext,baggage
    - OTEL_RESOURCE_ATTRIBUTES=environment=dev,team=ecommerce
  volumes:
    - ./opentelemetry-javaagent.jar:/app/opentelemetry-javaagent.jar:ro
  networks:
    - sparta-ecommerce-network
  depends_on:
    - mysql
    - redis
    - kafka
    - otel-collector
```

**Dockerfile 예시:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/ecommerce-service.jar app.jar
# Java Agent는 volume으로 마운트됨
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 방법 2: Spring Boot Dependency

### 특징
- ✅ **세밀한 제어**: 커스텀 Span, 속성 추가 가능
- ✅ **비즈니스 로직 추적**: 메서드 단위 추적
- ❌ 코드 수정 필요
- ❌ 설정 복잡

### 1단계: Dependency 추가

`build.gradle` (공통 모듈 또는 각 서비스):

```gradle
dependencies {
    // Spring Boot 3.x + OpenTelemetry
    implementation platform('io.opentelemetry:opentelemetry-bom:1.34.1')
    implementation 'io.opentelemetry:opentelemetry-api'
    implementation 'io.opentelemetry:opentelemetry-sdk'
    implementation 'io.opentelemetry:opentelemetry-exporter-otlp'

    // Spring Boot Actuator (관측성 통합)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Micrometer + OpenTelemetry Bridge
    implementation 'io.micrometer:micrometer-tracing-bridge-otel'
    implementation 'io.opentelemetry:opentelemetry-exporter-otlp-http-trace'
}
```

### 2단계: application.yml 설정

```yaml
spring:
  application:
    name: ecommerce-service  # 서비스 이름

management:
  # Actuator 엔드포인트
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

  # 트레이싱 설정
  tracing:
    enabled: true
    sampling:
      probability: 1.0  # 샘플링 비율 (1.0 = 100%, 운영: 0.1 = 10%)

  # OpenTelemetry OTLP Exporter
  otlp:
    tracing:
      endpoint: http://localhost:4317
      # HTTP 사용 시: http://localhost:4318/v1/traces
      compression: gzip
      timeout: 10s

  # 메트릭 설정 (선택사항)
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true

# 로그 패턴에 trace_id, span_id 추가
logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{trace_id},%X{span_id}]"
```

### 3단계: 커스텀 Span 추가 (비즈니스 로직 추적)

**자동 계측으로 추적되는 것:**
- HTTP 요청/응답
- JDBC 쿼리
- Kafka 메시지 발행/소비
- Redis 명령

**수동으로 추가해야 하는 것:**
- 비즈니스 메서드 (예: 주문 생성, 포인트 계산)
- 복잡한 계산 로직
- 외부 API 호출 (RestClient 등)

**예시 1: 메서드 추적**

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Tracer tracer;

    public OrderService(Tracer tracer) {
        this.tracer = tracer;
    }

    public Order createOrder(CreateOrderCommand command) {
        // 커스텀 Span 생성
        Span span = tracer.spanBuilder("OrderService.createOrder")
            .setAttribute("user.id", command.userId())
            .setAttribute("order.total_amount", command.totalAmount().toString())
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // 비즈니스 로직
            Order order = processOrder(command);

            // 추가 속성
            span.setAttribute("order.id", order.getId());
            span.setAttribute("order.status", order.getStatus().name());

            return order;
        } catch (Exception e) {
            // 에러 기록
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

**예시 2: 애노테이션 기반 (AOP 활용)**

```java
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class PointCalculator {

    @Observed(
        name = "point.calculation",
        contextualName = "calculate-earn-points",
        lowCardinalityKeyValues = {"category", "user_grade"}
    )
    public BigDecimal calculateEarnPoints(
        BigDecimal amount,
        String category,
        String userGrade
    ) {
        // 비즈니스 로직
        return amount.multiply(getEarnRate(category, userGrade));
    }
}
```

---

## 로그와 트레이스 연동

### Logback 설정 (MDC 활용)

`logback-spring.xml`:

```xml
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- OpenTelemetry MDC 추가 -->
    <property name="CONSOLE_LOG_PATTERN"
        value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [trace_id=%X{trace_id}, span_id=%X{span_id}] - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- Kafka Appender (ELK로 전송) -->
    <appender name="KAFKA" class="com.github.danielwegener.logback.kafka.KafkaAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <!-- trace_id, span_id 자동 포함 -->
            <includeMdcKeyName>trace_id</includeMdcKeyName>
            <includeMdcKeyName>span_id</includeMdcKeyName>
            <includeMdcKeyName>parent_span_id</includeMdcKeyName>
        </encoder>
        <topic>ecommerce.event.order.v1</topic>
        <keyingStrategy class="com.github.danielwegener.logback.kafka.keying.NoKeyKeyingStrategy"/>
        <deliveryStrategy class="com.github.danielwegener.logback.kafka.delivery.AsynchronousDeliveryStrategy"/>
        <producerConfig>bootstrap.servers=localhost:9092</producerConfig>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="KAFKA"/>
    </root>
</configuration>
```

### Kibana에서 로그 확인 후 Jaeger로 이동

1. **Kibana에서 에러 로그 검색**
   - URL: http://localhost:5601
   - 검색: `level:ERROR AND service:ecommerce-service`

2. **trace_id 확인**
   - 로그에서 `trace_id` 필드 복사

3. **Jaeger UI에서 트레이스 확인**
   - URL: http://localhost:16686
   - 검색창에 trace_id 입력
   - 전체 호출 체인 시각화 확인

---

## Kafka 이벤트 추적

### Context Propagation (컨텍스트 전파)

Kafka 메시지를 통해 trace_id를 전파하면 비동기 이벤트 흐름도 추적 가능합니다.

**Producer (auth-service):**

```java
@Service
public class UserEventPublisher {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
            user.getId(),
            user.getEmail(),
            user.getGrade().name()
        );

        // OpenTelemetry가 자동으로 trace context를 헤더에 추가
        kafkaTemplate.send("ecommerce.event.user.v1", user.getId().toString(), event);
    }
}
```

**Consumer (ecommerce-service):**

```java
@Service
public class UserEventListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @KafkaListener(
        topics = "ecommerce.event.user.v1",
        groupId = "ecommerce-service"
    )
    public void handleUserCreated(
        @Payload UserCreatedEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        // OpenTelemetry가 자동으로 trace context를 복원
        // 이 메서드의 span은 Producer의 span과 연결됨

        log.info("User created: userId={}", event.userId());

        // 포인트 지갑 생성
        createPointWallet(event);
    }
}
```

**Jaeger에서 확인:**
```
auth-service: POST /api/users/signup
  └─ kafka.send: ecommerce.event.user.v1
      └─ ecommerce-service: kafka.consume
          └─ PointWalletService.create
              └─ INSERT INTO point_wallets
```

---

## 트러블슈팅

### 1. 트레이스가 Jaeger에 표시되지 않음

**원인:** OTLP Endpoint 연결 실패

**확인:**
```bash
# OpenTelemetry Collector 상태 확인
docker logs sparta-ecommerce-otel-collector

# Jaeger 상태 확인
docker logs sparta-ecommerce-jaeger

# 포트 확인
curl http://localhost:4317
curl http://localhost:16686
```

**해결:**
- `OTEL_EXPORTER_OTLP_ENDPOINT` 환경변수 확인
- 네트워크 연결 확인 (Docker network)

### 2. 로그에 trace_id가 없음

**원인:** MDC 설정 누락

**확인:**
```java
import org.slf4j.MDC;

// 로그 출력 시 MDC 확인
log.info("trace_id: {}", MDC.get("trace_id"));
```

**해결:**
- `logback-spring.xml`에 MDC 패턴 추가
- `io.micrometer:micrometer-tracing-bridge-otel` dependency 확인

### 3. Kafka 메시지 추적 안됨

**원인:** Context Propagation 설정 누락

**해결:**
```yaml
# application.yml
management:
  tracing:
    propagation:
      type: W3C  # W3C Trace Context 표준
```

환경변수:
```bash
-Dotel.propagators=tracecontext,baggage
```

### 4. 성능 저하

**원인:** 100% 샘플링

**해결:**
```yaml
# application.yml - 운영 환경
management:
  tracing:
    sampling:
      probability: 0.1  # 10%만 샘플링
```

또는 환경변수:
```bash
-Dotel.traces.sampler=traceidratio
-Dotel.traces.sampler.arg=0.1
```

---

## 확인 방법

### 1. Docker 인프라 시작

```bash
cd /Users/joohyuk/Documents/study/project/project2/ai-commerce-platform/docker
docker-compose up -d

# 상태 확인
docker-compose ps
```

### 2. 서비스 상태 확인

- **OpenTelemetry Collector**: http://localhost:13133
- **Jaeger UI**: http://localhost:16686
- **Kibana**: http://localhost:5601
- **Kafka UI**: http://localhost:8088

### 3. 애플리케이션 실행 및 테스트

```bash
# 서비스 실행 (Java Agent 적용)
./gradlew :ecommerce-service:bootRun

# API 호출
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "items": [{"productId": 1, "quantity": 2}]}'
```

### 4. Jaeger에서 트레이스 확인

1. http://localhost:16686 접속
2. Service: `ecommerce-service` 선택
3. Operation: `POST /api/orders` 선택
4. "Find Traces" 클릭
5. 트레이스 클릭하여 상세 확인

**확인 사항:**
- ✅ HTTP 요청 span
- ✅ JDBC 쿼리 span (SELECT, INSERT, UPDATE)
- ✅ Kafka 메시지 발행 span
- ✅ Redis 명령 span
- ✅ 각 span의 duration (레이턴시)
- ✅ 에러 발생 시 error tag

### 5. Kibana에서 로그-트레이스 연동 확인

1. http://localhost:5601 접속
2. Discover 메뉴
3. 검색: `trace_id: *`
4. `jaeger_url` 필드 클릭
5. Jaeger UI로 자동 이동

---

## 모범 사례

### 1. Span 이름 규칙
- HTTP: `GET /api/orders`
- 메서드: `OrderService.createOrder`
- DB: `SELECT orders`
- Kafka: `kafka.send: topic-name`

### 2. 속성(Attributes) 추가
```java
span.setAttribute("user.id", userId);
span.setAttribute("order.total_amount", amount.toString());
span.setAttribute("http.status_code", 200);
```

### 3. 에러 기록
```java
catch (Exception e) {
    span.recordException(e);
    span.setStatus(StatusCode.ERROR, e.getMessage());
}
```

### 4. 샘플링 전략
- 개발: 100% (`probability: 1.0`)
- 스테이징: 50% (`probability: 0.5`)
- 운영: 10% (`probability: 0.1`)
- 에러는 항상 샘플링

---

## 다음 단계

1. ✅ **기본 설정 완료**: Java Agent 적용
2. ⏭️ **커스텀 Span 추가**: 비즈니스 로직 추적
3. ⏭️ **알림 설정**: Jaeger + Alertmanager 연동
4. ⏭️ **대시보드 구축**: Grafana + Prometheus 메트릭 시각화
5. ⏭️ **SLO 정의**: 서비스별 성능 목표 설정

---

## 참고 자료

- [OpenTelemetry 공식 문서](https://opentelemetry.io/docs/)
- [Jaeger 공식 문서](https://www.jaegertracing.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)
