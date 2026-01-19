# Event-Driven User Architecture

## 개요

MSA 아키텍처에서 auth-service와 ecommerce-service 간의 User 도메인 경계를 명확히 하고,
Event-Driven Architecture를 통해 느슨한 결합을 유지하면서 PointWallet 생성을 자동화합니다.

## 아키텍처 변경

### Before: Database per Service (중복 User 테이블)

```
┌──────────────────┐         ┌────────────────────┐
│  auth-service    │         │ ecommerce-service  │
├──────────────────┤         ├────────────────────┤
│ User DB ✓        │         │ User DB ✓ (중복!)  │
│ JWT/Passport     │         │ PointWallet DB     │
│                  │         │ Order DB           │
└──────────────────┘         └────────────────────┘
```

**문제점:**
- User 데이터 중복
- 동기화 이슈
- 명확하지 않은 소유권

### After: Event-Driven Architecture

```
┌──────────────────┐                    ┌────────────────────┐
│  auth-service    │                    │ ecommerce-service  │
│  (port 8083)     │                    │ (port 8082)        │
├──────────────────┤                    ├────────────────────┤
│ User DB ✓        │                    │                    │
│ ├─ users         │                    │ PointWallet DB ✓   │
│                  │                    │ ├─ point_wallets   │
│ RegisterUser     │                    │ └─ point_txns      │
│   ↓              │                    │                    │
│ UserCreatedEvent │──── Kafka ────────▶│ UserEventListener  │
│   publish        │ ecommerce.event    │   ↓                │
│                  │ .user.v1           │ create PointWallet │
└──────────────────┘                    └────────────────────┘

┌──────────────────────────────────────────────────────┐
│              API Gateway (port 8080)                 │
│  - Passport 발급 (auth-service 호출)                  │
│  - X-Passport 헤더로 ecommerce-service에 전달         │
└──────────────────────────────────────────────────────┘
```

**장점:**
- 명확한 소유권: User는 auth-service만 관리
- 느슨한 결합: Kafka 이벤트 기반 통신
- 확장성: 다른 서비스도 UserCreatedEvent 구독 가능
- Eventual Consistency 패턴 적용

## 구현 상세

### 1. auth-service: 이벤트 발행

#### 파일 구조
```
auth-service/
├── domain/event/
│   └── UserCreatedEvent.java          # 도메인 이벤트
├── domain/port/out/
│   └── UserEventPublisher.java        # Port 인터페이스
├── adapter/out/event/
│   ├── KafkaConfig.java               # Kafka Producer 설정
│   └── KafkaUserEventPublisher.java   # Kafka Adapter
└── application/service/
    └── RegisterUserService.java       # 이벤트 발행 로직
```

#### 주요 코드

**UserCreatedEvent** (auth-service/domain/event/UserCreatedEvent.java:1-29)
```java
@Getter
@Builder
public class UserCreatedEvent {
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String phoneNumber;
    private String grade;
    private LocalDateTime createdAt;
}
```

**RegisterUserService** (auth-service/application/service/RegisterUserService.java:62-74)
```java
Long userId = saveUserPort.save(user);

// User 생성 이벤트 발행
UserCreatedEvent event = UserCreatedEvent.builder()
    .userId(userId)
    .username(command.username())
    .email(command.email())
    // ...
    .build();

userEventPublisher.publishUserCreated(event);
```

**Kafka Topic**: `ecommerce.event.user.v1`

### 2. ecommerce-service: 이벤트 수신 및 PointWallet 생성

#### 파일 구조 (PointWallet 도메인)
```
ecommerce-service/
├── pointwallet/
│   ├── adapter/in/event/
│   │   ├── dto/
│   │   │   └── UserCreatedEvent.java          # Event DTO (Kafka 수신용)
│   │   └── PointWalletEventListener.java      # Kafka Listener (PointWallet 도메인)
│   └── domain/...
└── common/infrastructure/event/kafka/config/
    └── KafkaConsumerConfig.java               # Kafka Consumer 설정
```

**중요한 설계 원칙:**
- `PointWalletEventListener`는 **PointWallet 도메인**에 속함 (User 도메인 아님)
- UserCreatedEvent를 수신하지만, PointWallet을 생성하는 것이 핵심 책임
- ecommerce-service의 User 도메인은 최소화 (조회만 허용, 생성은 auth-service에서만)

#### 주요 코드

**PointWalletEventListener** (ecommerce-service/pointwallet/adapter/in/event/PointWalletEventListener.java:43-71)
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class PointWalletEventListener {  // PointWallet 도메인에 위치

    private final SavePointWalletPort savePointWalletPort;

    @KafkaListener(
        topics = "ecommerce.event.user.v1",
        groupId = "ecommerce-service",
        containerFactory = "userEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleUserCreated(UserCreatedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received UserCreatedEvent: userId={}, username={}, email={}",
                event.getUserId(), event.getUsername(), event.getEmail());

            // PointWallet 생성 (PointWallet 도메인의 핵심 책임)
            PointWallet wallet = PointWallet.createNew(event.getUserId());
            savePointWalletPort.save(wallet);

            log.info("PointWallet created successfully for userId: {}, balance: {}",
                event.getUserId(), wallet.getBalance().amount());

            // Kafka manual commit
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process UserCreatedEvent: userId={}, error={}",
                event.getUserId(), e.getMessage(), e);
            // 에러 발생 시 commit하지 않음 -> 재처리됨
            throw new BusinessException("Failed to process UserEvent", e, ErrorCode.MESSAGE_CONSUME_ERROR);
        }
    }
}
```

### 3. Fallback 메커니즘 (Eventual Consistency)

이벤트가 늦게 도착하거나 실패할 경우를 대비한 fallback:

**PointWalletQueryService** (ecommerce-service/application/service/PointWalletQueryService.java:31-32)
```java
public PointWalletResult getWallet(Long userId) {
    PointWallet wallet = loadPointWalletPort.findByUserId(userId)
        .orElseGet(() -> PointWallet.createNew(userId));  // Fallback!

    return PointWalletResult.from(wallet);
}
```

## 설정

### auth-service (application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9094
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### ecommerce-service (application.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9094
    # Producer 설정은 이미 존재 (OrderEvent용)
```

Consumer 설정은 KafkaConsumerConfig에서 프로그래밍 방식으로 설정됩니다.

## 사용 흐름

### 1. 회원가입 플로우

```
Client
  ↓ POST /api/v1/auth/register
auth-service
  ├─ User 생성 (auth DB)
  └─ UserCreatedEvent 발행 (Kafka)
       ↓ Topic: ecommerce.event.user.v1
ecommerce-service
  └─ UserEventListener
     └─ PointWallet 생성 (ecommerce DB)
```

**시간**:
- User 생성: ~50ms (동기)
- PointWallet 생성: ~10-100ms (비동기)

### 2. 주문 생성 플로우 (PointWallet 사용)

```
Client
  ↓ POST /api/v1/orders (with JWT)
API Gateway
  ├─ JWT 검증
  ├─ Passport 생성 (auth-service 또는 Redis 캐시)
  └─ X-Passport 헤더 추가
       ↓
ecommerce-service
  ├─ PassportContext.require() → userId
  ├─ PointWallet 조회 (findByUserId)
  │  └─ 없으면 즉시 생성 (fallback)
  └─ 주문 처리 및 포인트 적립
```

## 도메인 재구성 및 User 도메인 완전 제거 (MSA 원칙)

### ecommerce-service의 도메인 구조 변경

**Before (MSA 위반 구조):**
```
ecommerce-service/
├── user/                              ❌ User 도메인 전체 존재 (MSA 위반!)
│   ├── domain/entity/User.java
│   ├── adapter/out/security/...       ❌ CustomUserDetails 등
│   ├── adapter/out/persistence/...    ❌ User DB 조회
│   ├── application/service/...        ❌ User CRUD 서비스들
│   └── adapter/in/web/...             ❌ User API 엔드포인트
└── pointwallet/...
```

**After (올바른 MSA 구조):**
```
ecommerce-service/
├── pointwallet/adapter/in/event/
│   └── PointWalletEventListener.java  ✅ UserCreatedEvent 수신 → PointWallet 생성
├── common/auth/
│   └── Passport.java                  ✅ User 정보 (userId, grade 등)
└── [NO USER DOMAIN]                   ✅ User 도메인 완전 제거
```

**설계 원칙 (진정한 MSA):**
- **User 도메인은 auth-service에만 존재**: User 테이블, User CRUD는 auth-service 책임
- **Passport를 통한 User 정보 전달**: userId, grade, roles 등 필요한 정보만 Passport에 포함
- **이벤트 기반 데이터 전파**: UserCreatedEvent → PointWallet 생성 (비동기)
- **외부 API 호출로 User 정보 획득**: 추가 User 정보 필요 시 auth-service REST API 호출

### ecommerce-service에서 제거된 코드 (2026-01-19)

**완전 제거된 항목:**

1. **User 도메인 패키지 전체** (`ecommerce-service/src/main/java/com/spartaecommerce/user/`)
   - `domain/entity/User.java` - User 엔티티
   - `domain/entity/UserGrade.java` - UserGrade enum
   - `domain/repository/UserRepository.java` - User 레포지토리 인터페이스
   - `adapter/out/persistence/...` - User JPA 엔티티, Repository 구현
   - `application/service/` - GetUser, RegisterUser, UpdateUser, DeleteUser, SearchUsers 서비스들
   - `adapter/in/web/` - AuthController, UserController (User CRUD API)
   - `adapter/out/security/` - CustomUserDetails, CustomUserDetailService, Security 설정

2. **AuthenticationInterceptor** (`common/web/interceptor/AuthenticationInterceptor.java`)
   - CustomUserDetails 기반 인증 (레거시)
   - **PassportInterceptor로 완전 대체됨** (common/web/interceptor/PassportInterceptor.java)

3. **AuthenticatedUserArgumentResolver** (`common/web/resolver/AuthenticatedUserArgumentResolver.java`)
   - User 정보 주입 Resolver (레거시)
   - **PassportArgumentResolver로 완전 대체됨** (common/web/resolver/PassportArgumentResolver.java)

4. **cart의 LoadUserPort** (`cart/domain/port/out/LoadUserPort.java`)
   - Cart에서 User 조회 Port (미사용)

**수정된 설정:**

- `WebMvcConfig`: AuthenticationInterceptor, AuthenticatedUserArgumentResolver 제거
- 현재는 PassportInterceptor와 PassportArgumentResolver만 사용

## 트러블슈팅

### PointWallet이 생성되지 않음

**증상**: User는 생성되었지만 PointWallet이 없음

**원인**:
1. Kafka 연결 실패
2. Consumer가 실행되지 않음
3. 이벤트 처리 중 에러

**해결**:
```bash
# 1. Kafka 상태 확인
docker ps | grep kafka

# 2. Kafka 토픽 확인
kafka-console-consumer --bootstrap-server localhost:9094 \
  --topic ecommerce.event.user.v1 --from-beginning

# 3. ecommerce-service 로그 확인
# "Received UserCreatedEvent" 로그가 있는지 확인
```

### Eventual Consistency로 인한 일시적 불일치

**증상**: User 생성 직후 PointWallet 조회 시 404 또는 null

**해결**: 이미 구현된 fallback 메커니즘
```java
// PointWalletQueryService에서 자동 처리
.orElseGet(() -> PointWallet.createNew(userId))
```

### Kafka Consumer가 메시지를 재처리함

**원인**: 처리 중 에러 발생 시 commit하지 않음 (의도된 동작)

**해결**:
- 에러 로그 확인 및 수정
- Dead Letter Queue 구현 (향후)
- 최대 재시도 횟수 설정 (향후)

## 성능 고려사항

### Kafka 설정

| 항목 | 값 | 이유 |
|------|-----|------|
| `acks` | 1 | 리더 파티션만 확인 (성능과 안정성 균형) |
| `retries` | 3 | 일시적 장애 대응 |
| `enable.idempotence` | true | 중복 메시지 방지 |
| `auto.offset.reset` | earliest | Consumer 재시작 시 처음부터 |
| `enable.auto.commit` | false | Manual commit (트랜잭션 보장) |

### 파티셔닝 전략

```java
// UserId를 Key로 사용 → 같은 User의 이벤트는 순서 보장
String key = String.valueOf(event.getUserId());
kafkaTemplate.send(TOPIC, key, event);
```

## 확장 가능성

### 다른 서비스 추가 예시

```
┌──────────────────┐
│ notification-    │  ← UserCreatedEvent 구독
│ service          │     (환영 이메일 발송)
└──────────────────┘

┌──────────────────┐
│ analytics-       │  ← UserCreatedEvent 구독
│ service          │     (가입 통계 집계)
└──────────────────┘
```

각 서비스는 독립적으로 이벤트를 구독하고 처리할 수 있습니다.

## 참고 자료

- **Kafka 기반 Event-Driven Architecture**: Uber, Netflix 등에서 사용
- **Saga Pattern**: 분산 트랜잭션 관리 (향후 적용 가능)
- **CQRS**: Command/Query 분리 (이미 적용됨)
- **Eventual Consistency**: CAP 정리에서 선택한 전략

## 버전 히스토리

- **2026-01-19**: ecommerce-service User 도메인 완전 제거 (MSA 원칙 적용)
  - User 도메인 패키지 전체 제거 (45개 파일)
  - CustomUserDetails 기반 인증 → Passport 기반으로 완전 전환
  - AuthenticationInterceptor, AuthenticatedUserArgumentResolver 제거
  - cart의 LoadUserPort 제거
  - WebMvcConfig에서 PassportInterceptor만 사용하도록 간소화
  - **결과**: ecommerce-service는 User 정보를 Passport와 auth-service API로만 획득
  - 빌드 검증 완료 (컴파일 에러 없음)

- **2026-01-18**: Event-Driven User Architecture 구현
  - auth-service에 UserCreatedEvent 발행
  - ecommerce-service에 UserEventListener 추가
  - PointWallet 자동 생성 메커니즘 구축
  - ecommerce-service User 생성 기능 비활성화
