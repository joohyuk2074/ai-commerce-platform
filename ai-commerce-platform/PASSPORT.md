# Passport Authentication System (Phase 3)

Passport 기반 인증 시스템은 Netflix와 Toss의 아키텍처에서 영감을 받아 구현되었습니다.
Gateway가 Auth Service로부터 Passport를 받아 Backend 서비스에 전달함으로써, Backend 서비스가 Auth Service를 직접 호출할 필요가 없도록 합니다.

## 📋 목차

- [개요](#개요)
- [아키텍처](#아키텍처)
- [구성 요소](#구성-요소)
- [인증 흐름](#인증-흐름)
- [사용 방법](#사용-방법)
- [설정](#설정)
- [성능 최적화](#성능-최적화)
- [마이그레이션 가이드](#마이그레이션-가이드)

## 개요

### Passport란?

Passport는 사용자의 인증 정보를 담고 있는 일회성 인증 토큰입니다.
JWT 토큰을 검증한 후, 사용자의 상세 정보를 포함한 Passport를 생성하여 Backend 서비스에 전달합니다.

### 주요 특징

- **중앙화된 인증**: Gateway에서만 JWT 검증 수행
- **사용자 정보 전파**: Backend 서비스가 Auth Service 호출 없이 사용자 정보 접근
- **Redis 캐싱**: 55분 TTL로 Passport 캐싱하여 Auth Service 부하 감소
- **GZIP 압축**: HTTP 헤더 크기 제한(8KB) 대응

### 기존 JWT 방식 대비 장점

| 항목 | 기존 JWT 방식 | Passport (Phase 3) |
|------|--------------|-------------------|
| Auth Service 호출 | Backend 서비스에서 호출 필요 | Gateway에서만 호출 |
| 권한 변경 반영 | 토큰 만료 시까지 지연 | Passport 갱신으로 즉시 반영 |
| Backend 복잡도 | 각 서비스에서 인증 로직 필요 | PassportContext만 사용 |
| 네트워크 오버헤드 | 서비스마다 Auth 호출 가능 | 한 번의 Auth 호출 + Redis 캐싱 |

## 아키텍처

```
┌────────────┐
│   Client   │
└──────┬─────┘
       │ JWT Token (Authorization: Bearer xxx)
       ▼
┌─────────────────────────────────────────────┐
│           API Gateway (port 8080)           │
│  ┌──────────────────────────────────────┐  │
│  │  PassportAuthenticationStrategy      │  │
│  │  1. JWT 검증 (로컬)                  │  │
│  │  2. 블랙리스트 확인 (Redis)          │  │
│  │  3. PassportCacheService 호출        │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │  PassportCacheService                │  │
│  │  - Redis 캐시 확인                   │  │
│  │  - Cache Miss → Auth Service 호출    │  │
│  └──────────────────────────────────────┘  │
└─────────────────┬───────────────────────────┘
                  │ X-Passport: <Base64+GZIP>
                  ▼
       ┌──────────────────────┐
       │                      │
       ▼                      ▼
┌──────────────┐      ┌──────────────┐
│ ecommerce-   │      │ Auth Service │
│ service      │      │ (port 8083)  │
│ (port 8082)  │      │              │
│              │      │ POST /api/v1/│
│ Passport     │      │ auth/passport│
│ Interceptor  │      │              │
│ → Context    │      │ Passport     │
│              │      │ Service      │
└──────────────┘      └──────────────┘
```

## 구성 요소

### 1. Common Module

#### Passport (Model)
```java
public class Passport implements Serializable {
    private final Long userId;
    private final String username;
    private final String email;
    private final String grade;
    private final List<String> roles;
    private final Map<String, Object> metadata;
    private final Instant issuedAt;
    private final Instant expiresAt;      // 1시간 TTL
    private final String passportId;       // UUID
}
```

**파일 위치**: `common/src/main/java/com/spartaecommerce/common/auth/Passport.java`

#### PassportSerializer
- JSON 직렬화 → GZIP 압축 → Base64 인코딩
- HTTP 헤더 크기 제한(8KB) 대응

**파일 위치**: `common/src/main/java/com/spartaecommerce/common/auth/PassportSerializer.java`

#### PassportContext
- ThreadLocal을 사용한 Passport 저장소
- Service Layer에서 `PassportContext.require()` 호출로 사용자 정보 접근

**파일 위치**: `common/src/main/java/com/spartaecommerce/common/auth/PassportContext.java`

### 2. Auth Service

#### PassportService
JWT 토큰으로부터 Passport 생성:
1. JWT 검증
2. 사용자 ID 추출
3. User 엔티티 조회
4. Passport 생성 (권한, 메타데이터 포함)

**파일 위치**: `auth-service/src/main/java/com/spartaecommerce/auth/service/PassportService.java`

#### AuthController (Passport Endpoint)
```java
POST /api/v1/auth/passport
Header: Authorization: Bearer <JWT>
Response: {
  "success": true,
  "data": { /* Passport */ }
}
```

**파일 위치**: `auth-service/.../AuthController.java:92-103`

### 3. API Gateway

#### PassportCacheService
- Redis 캐싱: `passport:{userId}` 키로 55분 TTL
- Cache hit: Redis에서 직접 반환
- Cache miss: Auth Service 호출 → Redis 저장

**파일 위치**: `api-gateway/src/main/java/com/spartaecommerce/gateway/passport/PassportCacheService.java`

#### PassportAuthenticationStrategy
인증 전략:
1. JWT 토큰 검증 (로컬)
2. 블랙리스트 확인 (Redis)
3. PassportCacheService로 Passport 획득
4. Exchange 속성에 Passport 저장

**파일 위치**: `api-gateway/.../PassportAuthenticationStrategy.java`

#### AuthenticationGatewayFilterFactory
- Passport를 X-Passport 헤더로 직렬화하여 Backend에 전달

**파일 위치**: `api-gateway/.../AuthenticationGatewayFilterFactory.java:110-121`

### 4. Backend Services (ecommerce-service, auth-service)

#### PassportInterceptor
요청 처리 흐름:
1. `preHandle`: X-Passport 헤더 파싱 → PassportContext에 저장
2. `afterCompletion`: PassportContext 정리 (메모리 누수 방지)

**파일 위치**:
- `ecommerce-service/.../PassportInterceptor.java`
- `auth-service/.../PassportInterceptor.java`

#### Service Layer 사용 예시
```java
@Service
public class OrderService {

    public void createOrder(CreateOrderRequest request) {
        // PassportContext에서 사용자 정보 조회
        Passport passport = PassportContext.require();
        Long userId = passport.getUserId();
        String username = passport.getUsername();
        List<String> roles = passport.getRoles();

        // 비즈니스 로직...
    }
}
```

## 인증 흐름

### 1. 최초 요청 (Cache Miss)

```
Client → Gateway: GET /api/v1/orders (Authorization: Bearer xxx)
  ↓
Gateway: JWT 검증 (로컬)
  ↓
Gateway: Redis 블랙리스트 확인
  ↓
Gateway: Redis 캐시 확인 (passport:{userId})
  ↓ Cache Miss
Gateway → Auth Service: POST /api/v1/auth/passport
  ↓
Auth Service: JWT 검증 → User 조회 → Passport 생성
  ↓
Auth Service → Gateway: Passport
  ↓
Gateway: Redis에 Passport 저장 (TTL: 55분)
  ↓
Gateway → Backend: X-Passport 헤더 추가
  ↓
Backend: PassportInterceptor → PassportContext 저장
  ↓
Backend: Service Layer에서 PassportContext.require() 호출
```

**응답 시간**: ~100-200ms (Auth Service 호출 포함)

### 2. 후속 요청 (Cache Hit)

```
Client → Gateway: GET /api/v1/cart-items
  ↓
Gateway: JWT 검증 (로컬)
  ↓
Gateway: Redis 블랙리스트 확인
  ↓
Gateway: Redis 캐시 확인 (passport:{userId})
  ↓ Cache Hit!
Gateway → Backend: X-Passport 헤더 추가
  ↓
Backend: PassportInterceptor → PassportContext 저장
```

**응답 시간**: ~10-20ms (Auth Service 호출 없음)

## 사용 방법

### Swagger UI 테스트용 샘플

Swagger UI에서 API를 테스트할 때 사용할 수 있는 등급별 Passport 샘플이 필요하다면 [PASSPORT_SAMPLES.md](./PASSPORT_SAMPLES.md)를 참고하세요.

샘플에는 다음이 포함되어 있습니다:
- BRONZE, SILVER, GOLD, VIP 등급별 직렬화된 Passport 문자열
- ADMIN 권한을 가진 Passport
- Swagger UI에서 `X-Passport` 헤더로 사용하는 방법

### Service Layer에서 Passport 사용

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse createOrder(CreateOrderCommand command) {
        // 1. PassportContext에서 사용자 정보 조회
        Passport passport = PassportContext.require();
        Long userId = passport.getUserId();

        // 2. 비즈니스 로직
        Order order = Order.create(userId, command.getItems());
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }

    public void cancelOrder(Long orderId) {
        // 권한 확인 예시
        Passport passport = PassportContext.require();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUserId().equals(passport.getUserId())) {
            throw new UnauthorizedException("Not your order");
        }

        order.cancel();
    }
}
```

### Controller에서 사용 (선택적)

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        // PassportContext는 어디서든 사용 가능
        Passport passport = PassportContext.require();
        Long userId = passport.getUserId();

        // ...
    }
}
```

### Passport가 없는 경우 처리

```java
// Passport가 필수인 경우
Passport passport = PassportContext.require();  // PassportNotFoundException 발생

// Passport가 선택적인 경우
Passport passport = PassportContext.get();      // null 반환 가능
if (passport != null) {
    // 인증된 사용자 처리
} else {
    // 비인증 사용자 처리
}
```

## 설정

### API Gateway (`api-gateway/src/main/resources/application.yml`)

```yaml
# Authentication Configuration
auth:
  default-strategy: passport  # Passport 전략 사용

# Backend Services Configuration
services:
  auth:
    url: http://localhost:8083

# Redis Configuration (Passport Cache)
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Backend Services

WebMvcConfig에 PassportInterceptor가 자동으로 등록됩니다.

**ecommerce-service**: `ecommerce-service/.../WebMvcConfig.java:42-46`
**auth-service**: `auth-service/.../WebMvcConfig.java`

## 성능 최적화

### Redis 캐싱 전략

| 항목 | 값 | 설명 |
|------|-----|------|
| 캐시 키 | `passport:{userId}` | 사용자별 캐시 |
| TTL | 55분 | Passport 만료(1시간)보다 5분 짧게 |
| 압축 | GZIP | 네트워크 전송량 감소 |

### 성능 지표 (예상)

| 시나리오 | 응답 시간 | Auth Service 호출 |
|----------|-----------|-------------------|
| Cache Hit | 10-20ms | 없음 |
| Cache Miss | 100-200ms | 1회 |
| 동일 사용자 55분 내 | 10-20ms | 없음 |

### Passport 무효화

권한 변경, 사용자 정보 업데이트 시 캐시 무효화:

```java
@Service
public class UserService {

    private final PassportCacheService passportCacheService;

    public void updateUserRole(Long userId, String newRole) {
        // 사용자 권한 업데이트
        userRepository.updateRole(userId, newRole);

        // Passport 캐시 무효화
        passportCacheService.invalidatePassport(userId).subscribe();
    }
}
```

## 마이그레이션 가이드

### 기존 JWT 방식 → Passport (Phase 3)

#### 1. Gateway 설정

Gateway의 `application.yml`은 이미 Passport 전략을 사용하도록 설정되어 있습니다:

```yaml
auth:
  default-strategy: passport
```

#### 2. Service Layer 코드 변경 (권장)

기존 Spring Security 기반 코드는 계속 작동하지만, PassportContext를 사용하면 더 간결합니다:

```java
// Before: Spring Security 사용
@Service
public class OrderService {

    public void createOrder(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        // ...
    }
}

// After: PassportContext 사용
@Service
public class OrderService {

    public void createOrder() {
        Long userId = PassportContext.getUserId();  // 더 간결!
        // ...
    }
}
```

#### 3. 점진적 마이그레이션

- 기존 Authentication 기반 코드는 계속 작동
- PassportContext는 병행 사용 가능
- 새로운 코드부터 PassportContext 적용 권장

## 문제 해결 (Troubleshooting)

### Passport를 찾을 수 없음

```
PassportNotFoundException: Passport not found in context
```

**원인**:
- Gateway에서 X-Passport 헤더가 전달되지 않음
- PassportInterceptor가 등록되지 않음

**해결**:
1. Gateway 로그 확인: `Added X-Passport header for user`
2. Backend 로그 확인: `Passport loaded for user`
3. WebMvcConfig에서 PassportInterceptor 등록 확인

### Passport가 만료됨

```
WARN: Passport is expired: passportId=xxx, expiresAt=...
```

**원인**: Passport TTL(1시간) 초과

**해결**: Redis 캐시 무효화되므로 자동으로 새로운 Passport 생성됨

### Auth Service 호출 실패

```
ERROR: Failed to fetch Passport from Auth Service
```

**원인**: Auth Service 다운 또는 네트워크 문제

**해결**:
1. Auth Service 상태 확인
2. `services.auth.url` 설정 확인
3. 네트워크 연결 확인

## 참고 자료

- [Toss SLASH 23 - Server](https://toss.tech/article/slash23-server)
- Netflix Passport Architecture
- JWT vs Passport Trade-offs

## 버전 히스토리

- **Phase 3 (2026-01-17)**: Passport 시스템 도입 및 기존 JWT 전략 제거
- **Phase 2**: JWT Hybrid 전략 (Removed)
- **Phase 1**: Session + JWT 혼용 (Removed)
