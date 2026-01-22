# 관측성(Observability) 통합 아키텍처 가이드

## 목차
1. [개요](#개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [로그 수집 파이프라인](#로그-수집-파이프라인)
4. [분산 추적 파이프라인](#분산-추적-파이프라인)
5. [메트릭 수집 파이프라인](#메트릭-수집-파이프라인)
6. [통합 시나리오](#통합-시나리오)
7. [데이터 흐름 상세](#데이터-흐름-상세)

---

## 개요

### 관측성의 3가지 Pillar

본 프로젝트는 현대적인 마이크로서비스 관측성을 위한 **3가지 핵심 요소**를 통합 구현합니다:

| Pillar | 역할 | 도구 | 질문 답변 |
|--------|------|------|-----------|
| **Logs** | 이벤트 기록 | ELK Stack (Elasticsearch + Logstash + Kibana) | "무엇이 발생했는가?" |
| **Traces** | 요청 흐름 추적 | Jaeger + OpenTelemetry | "어디서 시간이 소요되는가?" |
| **Metrics** | 성능 지표 수집 | Prometheus + Grafana | "시스템이 얼마나 건강한가?" |

### 핵심 통합 포인트

```
로그 + 트레이스: trace_id를 통한 로그-트레이스 연결
트레이스 + 메트릭: span duration을 메트릭으로 집계
로그 + 메트릭: 로그 발생 빈도를 메트릭으로 추적
```

---

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Spring Boot Microservices                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ auth-service │  │  ecommerce   │  │   product    │  │    order     │   │
│  │   :8081      │  │   -service   │  │  -service    │  │  -service    │   │
│  │              │  │    :8082     │  │   :8083      │  │   :8084      │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                               │
│  [OpenTelemetry Java Agent]                                                  │
│   - 자동 계측: HTTP, JDBC, Kafka, Redis                                       │
│   - MDC에 trace_id, span_id 주입                                             │
│                                                                               │
│  [Logback Kafka Appender]                                                    │
│   - JSON 형식 로그 발행                                                       │
│   - trace_id, span_id 포함                                                   │
└──────┬────────────────────────┬──────────────────────────┬──────────────────┘
       │                        │                          │
       │ 로그                   │ 트레이스                  │ 메트릭
       │ (Kafka)               │ (OTLP gRPC)              │ (HTTP Scraping)
       │                        │                          │
       ▼                        ▼                          ▼
┌──────────────┐      ┌──────────────────┐      ┌──────────────────┐
│    Kafka     │      │ OpenTelemetry    │      │   Prometheus     │
│   :9092      │      │   Collector      │      │     :9090        │
│              │      │   :4317/4318     │      │                  │
│ Topics:      │      │                  │      │ Scrape Targets:  │
│ - audit      │      │ Receivers:       │      │ - otel-collector │
│ - order      │      │  - otlp (gRPC)   │      │ - services       │
└──────┬───────┘      │  - otlp (http)   │      └────────┬─────────┘
       │              │                  │               │
       │              │ Processors:      │               │
       │              │  - batch         │               │
       │              │  - memory_limiter│               │
       │              │                  │               │
       │              │ Exporters:       │               │
       │              │  - jaeger (gRPC) │               │
       │              │  - prometheus    │               │
       │              └────────┬─────────┘               │
       │                       │                         │
       ▼                       ▼                         │
┌──────────────┐      ┌──────────────────┐              │
│  Logstash    │      │     Jaeger       │              │
│   :5044      │      │   Backend        │              │
│              │      │   :14250 (gRPC)  │              │
│ Filters:     │      │   :16686 (UI)    │              │
│ - trace_id   │      │                  │              │
│ - span_id    │      │ Storage:         │              │
│ - jaeger_url │      │  Elasticsearch   │              │
└──────┬───────┘      └────────┬─────────┘              │
       │                       │                         │
       │                       │                         │
       ▼                       ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Elasticsearch :9200                         │
│                                                                  │
│  Indices:                                                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────┐  │
│  │ audit-user-      │  │ event-order-     │  │ jaeger-     │  │
│  │ activity-*       │  │ *                │  │ span-*      │  │
│  │                  │  │                  │  │             │  │
│  │ - clientIp       │  │ - service        │  │ - traceID   │  │
│  │ - userId         │  │ - trace_id ✅    │  │ - spanID    │  │
│  │ - method         │  │ - span_id ✅     │  │ - duration  │  │
│  │ - statusCode     │  │ - jaeger_url ✅  │  │ - tags      │  │
│  │ - trace_id ✅    │  │ - level          │  │             │  │
│  │ - jaeger_url ✅  │  │                  │  │             │  │
│  └──────────────────┘  └──────────────────┘  └─────────────┘  │
└──────┬─────────────────────────────────────────┬───────────────┘
       │                                         │
       ▼                                         ▼
┌──────────────┐                        ┌──────────────┐
│   Kibana     │                        │  Jaeger UI   │
│   :5601      │◄──trace_id 클릭────────│   :16686     │
│              │       연동              │              │
│ - 로그 검색   │                        │ - Trace 조회  │
│ - 대시보드    │                        │ - Span 분석   │
│ - 알림       │                        │ - 의존성 그래프│
└──────────────┘                        └──────────────┘
       ▲
       │
       │ Datasource
       │
┌──────────────┐
│   Grafana    │
│   :3000      │
│              │
│ Datasources: │
│ - Prometheus │
│ - Jaeger     │
│              │
│ Dashboards:  │
│ - 서비스 메트릭│
│ - 트레이스    │
│ - 통합 뷰    │
└──────────────┘
```

---

## 로그 수집 파이프라인

### 아키텍처 다이어그램

```
┌───────────────────────────────────────────────────────────────────┐
│               Spring Boot Application (8082)                       │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │  Business Logic                                           │    │
│  │  log.info("Order created: orderId={}", orderId)           │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                            │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │  Logback + MDC                                            │    │
│  │  - trace_id (from OpenTelemetry)                          │    │
│  │  - span_id (from OpenTelemetry)                           │    │
│  │  - parent_span_id                                         │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                            │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │  Kafka Appender (logback-kafka-appender)                  │    │
│  │  - Encoder: LogstashEncoder                               │    │
│  │  - Async delivery                                         │    │
│  └─────────────────────────┬────────────────────────────────┘    │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                             │ JSON 로그 발행
                             │
                             ▼
                    ┌──────────────────┐
                    │  Kafka :9092     │
                    │                  │
                    │  Topics:         │
                    │  ├─ ecommerce.   │
                    │  │  audit.user-  │
                    │  │  activity     │
                    │  └─ ecommerce.   │
                    │     event.order. │
                    │     v1           │
                    └────────┬─────────┘
                             │
                             │ Consumer Group: logstash-consumer-group
                             │ Consumer Threads: 3
                             │
                             ▼
           ┌──────────────────────────────────────┐
           │       Logstash :5044                  │
           │                                       │
           │  Input:                               │
           │  - Kafka consumer                     │
           │  - JSON codec                         │
           │                                       │
           │  Filter:                              │
           │  ├─ 토픽별 태그 추가                   │
           │  │  (audit / application)            │
           │  ├─ JSON 파싱                         │
           │  ├─ trace_id 정규화                   │
           │  │  (하이픈 제거)                     │
           │  ├─ jaeger_url 생성 ✅                │
           │  │  http://localhost:16686/trace/    │
           │  │  {trace_id}                       │
           │  ├─ 성능 등급 분류                     │
           │  │  (excellent/good/acceptable/poor) │
           │  ├─ HTTP 상태코드 분류                 │
           │  └─ 민감정보 마스킹                    │
           │                                       │
           │  Output:                              │
           │  - Elasticsearch                      │
           │  - stdout (debug)                     │
           └────────┬──────────────────────────────┘
                    │
                    │ Bulk indexing
                    │
                    ▼
    ┌─────────────────────────────────────────────┐
    │      Elasticsearch :9200                     │
    │                                              │
    │  Index: audit-user-activity-2026.01.22      │
    │  ┌────────────────────────────────────────┐ │
    │  │ {                                      │ │
    │  │   "@timestamp": "2026-01-22T10:00:00", │ │
    │  │   "service": "ecommerce",              │ │
    │  │   "userId": "1",                       │ │
    │  │   "method": "POST",                    │ │
    │  │   "path": "/api/orders",               │ │
    │  │   "statusCode": 200,                   │ │
    │  │   "responseTime": 150,                 │ │
    │  │   "clientIp": "127.0.0.1",             │ │
    │  │   "trace_id": "abc123...",  ✅         │ │
    │  │   "span_id": "def456...",   ✅         │ │
    │  │   "jaeger_url": "http://...", ✅       │ │
    │  │   "performance": "excellent"           │ │
    │  │ }                                      │ │
    │  └────────────────────────────────────────┘ │
    │                                              │
    │  Index: event-order-2026.01.22              │
    │  ┌────────────────────────────────────────┐ │
    │  │ {                                      │ │
    │  │   "@timestamp": "2026-01-22T10:00:01", │ │
    │  │   "level": "INFO",                     │ │
    │  │   "service": "ecommerce-service",      │ │
    │  │   "message": "Order created: ...",     │ │
    │  │   "trace_id": "abc123...",  ✅         │ │
    │  │   "span_id": "ghi789...",   ✅         │ │
    │  │   "jaeger_url": "http://...", ✅       │ │
    │  │   "thread": "http-nio-8082-exec-1"     │ │
    │  │ }                                      │ │
    │  └────────────────────────────────────────┘ │
    └──────────────────┬──────────────────────────┘
                       │
                       │ Query API
                       │
                       ▼
              ┌─────────────────┐
              │  Kibana :5601   │
              │                 │
              │  Features:      │
              │  ├─ Discover    │
              │  ├─ Dashboard   │
              │  ├─ Alerting    │
              │  └─ Visualize   │
              └─────────────────┘
```

### 로그 수집 흐름 설명

#### 1단계: 애플리케이션 로그 생성
```java
// OrderService.java
private final Logger log = LoggerFactory.getLogger(getClass());

public Order createOrder(CreateOrderCommand command) {
    // OpenTelemetry가 MDC에 trace_id, span_id 자동 주입
    log.info("Creating order: userId={}, totalAmount={}",
        command.userId(),
        command.totalAmount()
    );
    // ...
}
```

**MDC (Mapped Diagnostic Context) 자동 주입:**
```
trace_id: abc123def456789...
span_id: ghi789jkl012345...
parent_span_id: mno345pqr678901...
```

#### 2단계: Logback Kafka Appender로 발행
```xml
<!-- logback-spring.xml -->
<appender name="KAFKA" class="com.github.danielwegener.logback.kafka.KafkaAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>trace_id</includeMdcKeyName>
        <includeMdcKeyName>span_id</includeMdcKeyName>
        <includeMdcKeyName>parent_span_id</includeMdcKeyName>
    </encoder>
    <topic>ecommerce.event.order.v1</topic>
    <producerConfig>bootstrap.servers=kafka:9092</producerConfig>
</appender>
```

**발행되는 JSON 메시지:**
```json
{
  "@timestamp": "2026-01-22T10:00:01.234Z",
  "level": "INFO",
  "thread": "http-nio-8082-exec-1",
  "logger": "com.spartaecommerce.order.application.OrderService",
  "message": "Creating order: userId=1, totalAmount=50000",
  "trace_id": "abc123def456789",
  "span_id": "ghi789jkl012345"
}
```

#### 3단계: Logstash 필터링 및 변환
```ruby
# logstash.conf
filter {
  # trace_id 정규화 (하이픈 제거)
  if [trace_id] {
    mutate {
      gsub => ["trace_id", "-", ""]
      add_field => { "jaeger_trace_id" => "%{trace_id}" }
    }

    # Jaeger UI 링크 생성 ✅
    mutate {
      add_field => {
        "jaeger_url" => "http://localhost:16686/trace/%{trace_id}"
      }
    }
  }
}
```

#### 4단계: Elasticsearch 인덱싱
- **일별 인덱스 생성**: `event-order-2026.01.22`
- **검색 최적화**: trace_id, span_id 필드 인덱싱
- **Kibana 연동**: jaeger_url 필드를 클릭 가능한 링크로 표시

---

## 분산 추적 파이프라인

### 아키텍처 다이어그램

```
┌───────────────────────────────────────────────────────────────────┐
│               Spring Boot Application (8082)                       │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │  HTTP Request: POST /api/orders                           │    │
│  │  Headers: traceparent, tracestate (W3C Trace Context)     │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                            │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │  OpenTelemetry Java Agent (Auto-instrumentation)          │    │
│  │                                                            │    │
│  │  자동 계측 (Zero Code Change):                             │    │
│  │  ├─ HTTP Server/Client (Spring WebMVC)                    │    │
│  │  ├─ JDBC (MySQL)                                          │    │
│  │  ├─ Kafka Producer/Consumer                               │    │
│  │  ├─ Redis (Lettuce)                                       │    │
│  │  └─ RestClient/RestTemplate                               │    │
│  │                                                            │    │
│  │  생성되는 Span:                                            │    │
│  │  ├─ POST /api/orders [parent]                             │    │
│  │  │  ├─ SELECT products [child]                            │    │
│  │  │  ├─ UPDATE products SET stock [child]                  │    │
│  │  │  ├─ INSERT INTO orders [child]                         │    │
│  │  │  ├─ kafka.send: ecommerce.event.order.v1 [child]       │    │
│  │  │  └─ redis.set: cart:user:1 [child]                     │    │
│  │                                                            │    │
│  │  MDC 주입: trace_id, span_id → Logback                     │    │
│  └─────────────────────────┬────────────────────────────────┘    │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                             │ OTLP (OpenTelemetry Protocol)
                             │ gRPC: 4317 (default)
                             │ HTTP: 4318 (alternative)
                             │
                             ▼
           ┌──────────────────────────────────────┐
           │  OpenTelemetry Collector :4317/4318  │
           │                                       │
           │  Receivers:                           │
           │  ├─ otlp (gRPC): 0.0.0.0:4317         │
           │  └─ otlp (http): 0.0.0.0:4318         │
           │                                       │
           │  Processors:                          │
           │  ├─ batch (배치 처리로 성능 최적화)     │
           │  │  - timeout: 10s                    │
           │  │  - send_batch_size: 1024           │
           │  ├─ memory_limiter (메모리 보호)       │
           │  │  - limit_mib: 512                  │
           │  └─ attributes (태그 추가)             │
           │     - environment: dev                │
           │     - platform: sparta-ecommerce      │
           │                                       │
           │  Exporters:                           │
           │  ├─ jaeger (gRPC): jaeger:14250       │
           │  └─ prometheus: :8888/metrics         │
           │                                       │
           │  Health Check: :13133                 │
           └────────┬──────────────────────────────┘
                    │
                    │ Jaeger gRPC Protocol
                    │ Protobuf 직렬화
                    │
                    ▼
    ┌─────────────────────────────────────────────┐
    │      Jaeger Backend :14250 (gRPC)            │
    │                                              │
    │  Components:                                 │
    │  ├─ Collector (데이터 수신)                  │
    │  ├─ Query Service (조회 API)                 │
    │  └─ UI (시각화)                              │
    │                                              │
    │  Storage Backend:                            │
    │  - Elasticsearch :9200                       │
    │  - Index Prefix: jaeger                      │
    │                                              │
    │  Performance Tuning:                         │
    │  - Queue Size: 2000                          │
    │  - Workers: 50                               │
    └──────────────────┬──────────────────────────┘
                       │
                       │ Elasticsearch Index API
                       │
                       ▼
    ┌─────────────────────────────────────────────┐
    │      Elasticsearch :9200                     │
    │                                              │
    │  Index: jaeger-span-2026-01-22              │
    │  ┌────────────────────────────────────────┐ │
    │  │ {                                      │ │
    │  │   "traceID": "abc123def456...",        │ │
    │  │   "spanID": "ghi789jkl012...",         │ │
    │  │   "operationName": "POST /api/orders", │ │
    │  │   "startTime": 1674378000000000,       │ │
    │  │   "duration": 150000,  // microseconds │ │
    │  │   "tags": [                            │ │
    │  │     { "key": "http.method",            │ │
    │  │       "value": "POST" },               │ │
    │  │     { "key": "http.status_code",       │ │
    │  │       "value": 200 },                  │ │
    │  │     { "key": "db.system",              │ │
    │  │       "value": "mysql" }               │ │
    │  │   ],                                   │ │
    │  │   "references": [                      │ │
    │  │     { "refType": "CHILD_OF",           │ │
    │  │       "traceID": "abc123...",          │ │
    │  │       "spanID": "parent123..." }       │ │
    │  │   ]                                    │ │
    │  │ }                                      │ │
    │  └────────────────────────────────────────┘ │
    └──────────────────┬──────────────────────────┘
                       │
                       │ Query API
                       │
                       ▼
              ┌─────────────────┐
              │ Jaeger UI :16686│
              │                 │
              │  Features:      │
              │  ├─ Search      │
              │  ├─ Timeline    │
              │  ├─ Dependencies│
              │  └─ Compare     │
              └─────────────────┘
```

### 분산 추적 흐름 설명

#### 1단계: 요청 시작 (Trace Context 생성)

**최초 요청 (외부 → auth-service):**
```http
POST /api/users/signup HTTP/1.1
Host: localhost:8081
```

**OpenTelemetry Agent가 자동 생성:**
- **Trace ID**: 128-bit 랜덤 ID (전체 요청 흐름 식별)
- **Span ID**: 64-bit 랜덤 ID (현재 작업 단위 식별)
- **W3C Trace Context Header** 생성:
```http
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
             └─ trace-id ──────────────────────┘ └─ span-id ────┘└─ flags
```

#### 2단계: Context Propagation (서비스 간 전파)

**auth-service → Kafka → ecommerce-service:**

```java
// auth-service: UserEventPublisher.java
public void publishUserCreated(User user) {
    UserCreatedEvent event = new UserCreatedEvent(...);

    // OpenTelemetry가 자동으로 Kafka 헤더에 trace context 추가
    kafkaTemplate.send("ecommerce.event.user.v1", event);
    // Kafka Message Headers:
    // traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-...
}
```

```java
// ecommerce-service: UserEventListener.java
@KafkaListener(topics = "ecommerce.event.user.v1")
public void handleUserCreated(UserCreatedEvent event) {
    // OpenTelemetry가 자동으로 Kafka 헤더에서 trace context 복원
    // 이 메서드의 span은 Producer의 span과 부모-자식 관계로 연결됨

    log.info("User created: userId={}", event.userId());
    // MDC에 trace_id 자동 주입 → 로그에 포함
}
```

#### 3단계: Span 생성 및 수집

**자동 생성되는 Span 계층 구조:**

```
POST /api/users/signup (auth-service)
  duration: 250ms
  tags: http.method=POST, http.status_code=200
  │
  ├─ INSERT INTO users (MySQL)
  │  duration: 30ms
  │  tags: db.system=mysql, db.statement=INSERT INTO users...
  │
  ├─ redis.set: session:user:1
  │  duration: 5ms
  │  tags: db.system=redis, redis.command=SET
  │
  └─ kafka.send: ecommerce.event.user.v1
     duration: 10ms
     tags: messaging.system=kafka, messaging.destination=ecommerce.event.user.v1
     │
     └─ kafka.consume: ecommerce.event.user.v1 (ecommerce-service)
        duration: 180ms
        tags: messaging.system=kafka
        │
        ├─ PointWalletService.create
        │  duration: 50ms
        │  │
        │  └─ INSERT INTO point_wallets (MySQL)
        │     duration: 25ms
```

#### 4단계: OTLP로 데이터 전송

**전송 프로토콜:**
```protobuf
// OpenTelemetry Protocol (OTLP)
ResourceSpans {
  resource: {
    attributes: {
      service.name: "auth-service",
      environment: "dev"
    }
  },
  scopeSpans: [{
    spans: [
      {
        traceId: "4bf92f3577b34da6a3ce929d0e0e4736",
        spanId: "00f067aa0ba902b7",
        name: "POST /api/users/signup",
        kind: SPAN_KIND_SERVER,
        startTimeUnixNano: 1674378000000000000,
        endTimeUnixNano: 1674378000250000000,
        attributes: [
          { key: "http.method", value: "POST" },
          { key: "http.status_code", value: 200 }
        ]
      }
    ]
  }]
}
```

#### 5단계: Jaeger UI에서 시각화

**Timeline View:**
```
auth-service: POST /api/users/signup ━━━━━━━━━━━━━━━━━━━━━━━ 250ms
  ├─ INSERT INTO users ━━━ 30ms
  ├─ redis.set ━ 5ms
  └─ kafka.send ━━ 10ms
      └─ ecommerce-service: kafka.consume ━━━━━━━━━━━━━ 180ms
          └─ PointWalletService.create ━━━━━━ 50ms
              └─ INSERT INTO point_wallets ━━━ 25ms
```

---

## 메트릭 수집 파이프라인

### 아키텍처 다이어그램

```
┌───────────────────────────────────────────────────────────────────┐
│               Spring Boot Application (8082)                       │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │  Spring Boot Actuator + Micrometer                        │    │
│  │                                                            │    │
│  │  자동 수집 메트릭:                                          │    │
│  │  ├─ JVM 메트릭                                             │    │
│  │  │  - jvm.memory.used                                     │    │
│  │  │  - jvm.gc.pause                                        │    │
│  │  │  - jvm.threads.live                                    │    │
│  │  ├─ HTTP 메트릭                                            │    │
│  │  │  - http.server.requests (duration, count)              │    │
│  │  │  - http.server.requests.active                         │    │
│  │  ├─ 데이터베이스 메트릭                                     │    │
│  │  │  - hikaricp.connections.active                         │    │
│  │  │  - hikaricp.connections.pending                        │    │
│  │  ├─ Kafka 메트릭                                           │    │
│  │  │  - kafka.producer.request.total                        │    │
│  │  │  - kafka.consumer.fetch.total                          │    │
│  │  └─ Redis 메트릭                                           │    │
│  │     - lettuce.command.completion.time                     │    │
│  │                                                            │    │
│  │  커스텀 메트릭:                                            │    │
│  │  ├─ order.created.total (Counter)                         │    │
│  │  ├─ order.amount.sum (Summary)                            │    │
│  │  └─ order.processing.time (Timer)                         │    │
│  └─────────────────────────┬────────────────────────────────┘    │
│                            │                                      │
│  ┌─────────────────────────▼────────────────────────────────┐    │
│  │  /actuator/prometheus                                     │    │
│  │  HTTP Endpoint: :8082/actuator/prometheus                 │    │
│  │                                                            │    │
│  │  Prometheus Text Format:                                  │    │
│  │  # TYPE http_server_requests_seconds histogram            │    │
│  │  http_server_requests_seconds_bucket{                     │    │
│  │    method="POST",uri="/api/orders",le="0.1"               │    │
│  │  } 45                                                      │    │
│  └────────────────────────────────────────────────────────────    │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                             │ HTTP Scraping (Pull Model)
                             │ Interval: 15s
                             │
                             ▼
           ┌──────────────────────────────────────┐
           │      Prometheus :9090                 │
           │                                       │
           │  Scrape Targets:                      │
           │  ├─ ecommerce-service:8082/actuator/  │
           │  │  prometheus                        │
           │  ├─ auth-service:8081/actuator/       │
           │  │  prometheus                        │
           │  ├─ product-service:8083/actuator/    │
           │  │  prometheus                        │
           │  ├─ order-service:8084/actuator/      │
           │  │  prometheus                        │
           │  └─ otel-collector:8888/metrics       │
           │                                       │
           │  Storage:                             │
           │  - TSDB (Time Series Database)        │
           │  - Retention: 30 days                 │
           │  - Compression: ~1.3 bytes/sample     │
           │                                       │
           │  Query Engine: PromQL                 │
           └────────┬──────────────────────────────┘
                    │
                    │ HTTP API: /api/v1/query
                    │ PromQL 지원
                    │
                    ▼
              ┌─────────────────┐
              │  Grafana :3000  │
              │                 │
              │  Datasource:    │
              │  - Prometheus   │
              │  - Jaeger       │
              │                 │
              │  Dashboards:    │
              │  ├─ JVM 대시보드│
              │  ├─ HTTP 메트릭 │
              │  ├─ 비즈니스    │
              │  └─ 통합 뷰     │
              └─────────────────┘

┌─────────────────────────────────────────────────────────┐
│  OpenTelemetry Collector :8888/metrics                  │
│                                                          │
│  자체 메트릭 (Prometheus format):                        │
│  ├─ otelcol_receiver_accepted_spans                     │
│  ├─ otelcol_exporter_sent_spans                         │
│  ├─ otelcol_processor_batch_batch_send_size             │
│  └─ otelcol_exporter_queue_size                         │
│                                                          │
│  Span 메트릭 변환 (Span → Metric):                       │
│  ├─ http_server_request_duration_seconds                │
│  │  (Span duration → Histogram)                         │
│  ├─ http_server_request_total                           │
│  │  (Span count → Counter)                              │
│  └─ db_query_duration_seconds                           │
│     (DB span duration → Histogram)                      │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ Scraped by Prometheus
                   │
                   ▼
         (Prometheus로 수집됨)
```

### 메트릭 수집 흐름 설명

#### 1단계: 애플리케이션 메트릭 생성

**자동 메트릭 (Spring Boot Actuator):**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true  # 히스토그램 활성화
```

**커스텀 메트릭 (Micrometer):**
```java
@Service
public class OrderService {
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;

    public OrderService(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("order.created.total")
            .tag("service", "ecommerce")
            .description("Total orders created")
            .register(registry);

        this.orderProcessingTimer = Timer.builder("order.processing.time")
            .tag("service", "ecommerce")
            .description("Order processing time")
            .register(registry);
    }

    public Order createOrder(CreateOrderCommand command) {
        return orderProcessingTimer.record(() -> {
            Order order = processOrder(command);
            orderCreatedCounter.increment();
            return order;
        });
    }
}
```

#### 2단계: Prometheus Endpoint 노출

**/actuator/prometheus 응답 예시:**
```
# HELP http_server_requests_seconds HTTP 요청 응답 시간
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{method="POST",uri="/api/orders",status="200",le="0.05"} 10
http_server_requests_seconds_bucket{method="POST",uri="/api/orders",status="200",le="0.1"} 45
http_server_requests_seconds_bucket{method="POST",uri="/api/orders",status="200",le="0.5"} 98
http_server_requests_seconds_bucket{method="POST",uri="/api/orders",status="200",le="+Inf"} 100
http_server_requests_seconds_count{method="POST",uri="/api/orders",status="200"} 100
http_server_requests_seconds_sum{method="POST",uri="/api/orders",status="200"} 12.5

# HELP jvm_memory_used_bytes JVM 메모리 사용량
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 134217728
jvm_memory_used_bytes{area="heap",id="PS Old Gen"} 268435456

# HELP order_created_total 생성된 주문 총 수
# TYPE order_created_total counter
order_created_total{service="ecommerce"} 1523
```

#### 3단계: Prometheus Scraping

**prometheus.yml 설정:**
```yaml
scrape_configs:
  - job_name: 'spring-boot-services'
    scrape_interval: 15s  # 15초마다 수집
    scrape_timeout: 10s
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'host.docker.internal:8081'  # auth-service
          - 'host.docker.internal:8082'  # ecommerce-service
          - 'host.docker.internal:8083'  # product-service
          - 'host.docker.internal:8084'  # order-service
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
```

#### 4단계: Grafana 대시보드 시각화

**PromQL 쿼리 예시:**

**1) HTTP 요청 속도 (RPS - Requests Per Second):**
```promql
rate(http_server_requests_seconds_count{uri="/api/orders"}[5m])
```

**2) HTTP 요청 P95 레이턴시:**
```promql
histogram_quantile(0.95,
  rate(http_server_requests_seconds_bucket{uri="/api/orders"}[5m])
)
```

**3) 에러율:**
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/
sum(rate(http_server_requests_seconds_count[5m]))
```

**4) JVM 힙 메모리 사용률:**
```promql
jvm_memory_used_bytes{area="heap"}
/
jvm_memory_max_bytes{area="heap"}
* 100
```

**5) 데이터베이스 커넥션 풀 사용률:**
```promql
hikaricp_connections_active
/
hikaricp_connections_max
* 100
```

---

## 통합 시나리오

### 시나리오 1: 로그에서 트레이스로 이동

```
[사용자 행동]
Kibana에서 에러 로그 발견 → trace_id 클릭 → Jaeger UI로 자동 이동

[상세 흐름]
1. Kibana Discover에서 검색
   - Query: level:ERROR AND service:ecommerce-service
   - 결과: "Order creation failed: Insufficient stock"

2. 로그 상세 보기
   {
     "timestamp": "2026-01-22T10:00:01",
     "level": "ERROR",
     "message": "Order creation failed: Insufficient stock",
     "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",  ✅
     "jaeger_url": "http://localhost:16686/trace/4bf92f3577b34da6a3ce929d0e0e4736"  ✅
   }

3. jaeger_url 클릭 → 새 탭에서 Jaeger UI 열림

4. Jaeger UI에서 전체 트레이스 확인
   - 어느 단계에서 에러 발생했는지 시각화
   - 각 Span의 duration 확인
   - 에러 태그 및 스택 트레이스 확인
```

**Jaeger UI에서 보이는 Timeline:**
```
POST /api/orders ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 250ms ❌ ERROR
  ├─ SELECT products ━━━━━━ 30ms ✅
  ├─ Stock validation ━━━ 10ms ✅
  └─ UPDATE products SET stock ━━━━━━━━━━━━━━━━━━━━━━━━ 200ms ❌ ERROR
      └─ Error: Insufficient stock for product_id=123
```

### 시나리오 2: 트레이스에서 메트릭으로 분석

```
[사용자 행동]
Jaeger에서 특정 엔드포인트 느림 발견 → Grafana에서 메트릭 추세 확인

[상세 흐름]
1. Jaeger UI에서 느린 트레이스 발견
   - Operation: POST /api/orders
   - Duration: 5.2초 (평균 300ms보다 매우 느림)
   - 원인: MySQL SELECT products 쿼리가 4.8초 소요

2. Grafana 대시보드로 이동

3. PromQL로 패턴 분석
   # 최근 1시간 P95 레이턴시 추세
   histogram_quantile(0.95,
     rate(http_server_requests_seconds_bucket{uri="/api/orders"}[1h])
   )

   # DB 커넥션 풀 상태 확인
   hikaricp_connections_active
   hikaricp_connections_pending

4. 원인 파악
   - DB 커넥션 풀이 고갈됨 (active=10/10, pending=50)
   - 느린 쿼리가 커넥션을 오래 점유
```

### 시나리오 3: 메트릭 알림 → 로그 → 트레이스

```
[사용자 행동]
Prometheus Alerting → Kibana에서 로그 확인 → Jaeger에서 트레이스 분석

[상세 흐름]
1. Prometheus Alert 발생
   Alert: HighErrorRate
   Severity: critical
   Summary: Error rate > 5% for /api/orders

2. Kibana로 이동하여 에러 로그 검색
   Query:
     level:ERROR
     AND service:ecommerce-service
     AND @timestamp:[now-15m TO now]

3. 공통 패턴 발견
   - 모든 에러가 동일한 trace_id 패턴
   - 특정 product_id에서만 발생

4. Jaeger에서 trace_id 조회
   - 재고 부족 에러 확인
   - 재고 업데이트 로직에 동시성 문제 발견

5. 근본 원인 해결
   - 분산 락 적용
   - 재배포 후 메트릭 개선 확인
```

---

## 데이터 흐름 상세

### Trace ID의 생명주기

```
1. 생성 (auth-service)
   ┌─────────────────────────────────────┐
   │ OpenTelemetry Agent                 │
   │ trace_id: 4bf92f35...  (128-bit)    │
   │ span_id: 00f067aa...   (64-bit)     │
   └─────────────────┬───────────────────┘
                     │
   2. MDC 주입 (Logback)
   ┌─────────────────▼───────────────────┐
   │ MDC.put("trace_id", "4bf92f35...")  │
   │ log.info("User created")            │
   └─────────────────┬───────────────────┘
                     │
   3. Kafka 메시지 헤더에 전파
   ┌─────────────────▼───────────────────┐
   │ Kafka Message Headers:              │
   │ traceparent: 00-4bf92f35...-...     │
   └─────────────────┬───────────────────┘
                     │
   4. Consumer에서 복원 (ecommerce-service)
   ┌─────────────────▼───────────────────┐
   │ OpenTelemetry Agent                 │
   │ 헤더에서 trace_id 복원               │
   │ 새로운 span_id 생성 (자식 Span)      │
   │ parent_span_id = 00f067aa...        │
   └─────────────────┬───────────────────┘
                     │
   5. 로그 발행 (Kafka)
   ┌─────────────────▼───────────────────┐
   │ Kafka: ecommerce.event.order.v1     │
   │ {                                   │
   │   "trace_id": "4bf92f35...",        │
   │   "span_id": "abc123...",           │
   │   "parent_span_id": "00f067aa..."   │
   │ }                                   │
   └─────────────────┬───────────────────┘
                     │
   6. Logstash 처리
   ┌─────────────────▼───────────────────┐
   │ Logstash Filter                     │
   │ - trace_id 하이픈 제거               │
   │ - jaeger_url 생성                   │
   │   http://localhost:16686/trace/     │
   │   4bf92f35...                       │
   └─────────────────┬───────────────────┘
                     │
   7. Elasticsearch 저장
   ┌─────────────────▼───────────────────┐
   │ Index: event-order-2026.01.22       │
   │ {                                   │
   │   "trace_id": "4bf92f35...",  ✅    │
   │   "jaeger_url": "http://..."  ✅    │
   │ }                                   │
   └─────────────────────────────────────┘

   8. OTLP로 Span 전송
   ┌─────────────────────────────────────┐
   │ OpenTelemetry Collector             │
   │ → Jaeger Backend                    │
   │ → Elasticsearch (jaeger-span-*)     │
   │ {                                   │
   │   "traceID": "4bf92f35...",         │
   │   "spans": [...]                    │
   │ }                                   │
   └─────────────────────────────────────┘

   9. 시각화
   ┌─────────────────────────────────────┐
   │ Kibana: 로그 조회 → trace_id 클릭    │
   │           ↓                         │
   │ Jaeger UI: 전체 트레이스 시각화      │
   └─────────────────────────────────────┘
```

### 데이터 저장소 비교

| 저장소 | 데이터 유형 | 보관 기간 | 인덱스 패턴 | 쿼리 언어 |
|--------|-----------|----------|------------|----------|
| **Elasticsearch** | 로그, 트레이스 | 제한 없음 (디스크 공간에 따름) | `audit-*`, `event-*`, `jaeger-*` | Elasticsearch Query DSL |
| **Prometheus** | 메트릭 | 30일 (설정 가능) | N/A (TSDB) | PromQL |
| **Jaeger (ES 백엔드)** | 트레이스 | Elasticsearch와 동일 | `jaeger-span-*`, `jaeger-service-*` | Jaeger Query API |

### 포트 맵핑 전체

| 서비스 | 포트 | 용도 |
|--------|------|------|
| **MySQL** | 3306 | 데이터베이스 |
| **Redis** | 6379 | 캐시 및 분산 락 |
| **Kafka** | 9092 (내부), 9094 (외부) | 메시지 브로커 |
| **Elasticsearch** | 9200 (HTTP), 9300 (Transport) | 로그/트레이스 저장 |
| **Logstash** | 5044 (Beats), 9600 (API) | 로그 처리 |
| **Kibana** | 5601 | 로그 시각화 |
| **Kafka UI** | 8088 | Kafka 모니터링 |
| **OpenTelemetry Collector** | 4317 (gRPC), 4318 (HTTP), 13133 (Health) | 텔레메트리 수집 |
| **Jaeger UI** | 16686 | 트레이스 시각화 |
| **Jaeger Collector** | 14250 (gRPC) | 트레이스 수신 |
| **Prometheus** | 9090 | 메트릭 수집/조회 |
| **Grafana** | 3000 | 통합 대시보드 |
| **auth-service** | 8081 | 인증 서비스 |
| **ecommerce-service** | 8082 | 이커머스 서비스 |
| **product-service** | 8083 | 상품 서비스 |
| **order-service** | 8084 | 주문 서비스 |

---

## 다음 단계

1. ✅ **인프라 구성 완료**: Docker Compose로 모든 컴포넌트 실행
2. ⏭️ **애플리케이션 통합**: OpenTelemetry Java Agent 적용
3. ⏭️ **Grafana 대시보드 구축**: 서비스별 메트릭 시각화
4. ⏭️ **알림 설정**: Prometheus Alertmanager 연동
5. ⏭️ **SLO 정의**: 서비스 수준 목표 및 에러 버짓 설정

---

## 참고 자료

- [OpenTelemetry 공식 문서](https://opentelemetry.io/docs/)
- [Jaeger 공식 문서](https://www.jaegertracing.io/docs/)
- [Prometheus 공식 문서](https://prometheus.io/docs/)
- [Grafana 공식 문서](https://grafana.com/docs/)
- [ELK Stack 가이드](https://www.elastic.co/guide/)
- [W3C Trace Context 표준](https://www.w3.org/TR/trace-context/)
- [Micrometer 문서](https://micrometer.io/docs/)
