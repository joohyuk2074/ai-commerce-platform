# API Gateway

E-commerce 플랫폼의 API Gateway 서비스입니다. 모든 클라이언트 요청의 진입점 역할을 하며, 라우팅, 인증, 권한 체크 등의 기능을 제공합니다.

## 주요 기능

### 1. 도메인별 라우팅
각 도메인별로 세분화된 라우팅 규칙:
- `/api/v1/auth/**` - 인증 (회원가입, 로그인)
- `/api/v1/users/**` - 사용자 관리
- `/api/v1/products/**` - 상품 관리
- `/api/v1/categories/**` - 카테고리 관리
- `/api/v1/cart-items/**` - 장바구니
- `/api/v1/orders/**` - 주문
- `/api/v1/points/**` - 포인트 지갑
- `/api/v1/refunds/**` - 환불

### 2. 계층화된 권한 체크

#### Coarse-grained 권한 (Gateway 레벨)
Gateway에서 metadata의 `access-level`을 기반으로 기본 권한 체크:

- **public**: 인증 불필요 (누구나 접근 가능)
  - 예: 회원가입, 로그인

- **public-read-admin-write**: 조회는 public, 쓰기는 admin
  - GET 요청: 인증 불필요
  - POST/PUT/PATCH/DELETE: ADMIN 권한 필요
  - 예: 상품 조회는 누구나, 상품 등록/수정은 관리자만

- **authenticated**: 인증된 사용자만 접근
  - 예: 장바구니, 주문, 포인트, 환불

- **admin**: 관리자만 접근

#### Fine-grained 권한 (Service 레벨)
각 서비스에서 세밀한 권한 체크:
- 리소스 소유 여부 확인 (예: 사용자 자신의 주문만 조회)
- 상태별 제약 (예: 배송 중인 주문은 취소 불가)
- 도메인 규칙 (예: 포인트 최소 사용 금액)

### 3. 확장 가능한 인증 전략

전략 패턴을 사용하여 인증 방식을 쉽게 교체 가능:

#### 현재: 세션 기반 인증
```yaml
auth:
  strategy: session
```
- JSESSIONID 쿠키를 확인하여 인증 상태 검증
- ecommerce-service의 세션 인증과 통합

#### 향후: JWT 기반 인증
```yaml
auth:
  strategy: jwt
```
- Authorization: Bearer {token} 헤더에서 JWT 추출
- JWT 검증 및 Claims 파싱
- 마이크로서비스 환경에 적합

### 4. 인증 정보 전달

인증 성공 시 downstream 서비스로 사용자 정보를 헤더로 전달:
- `X-User-Id`: 사용자 ID
- `X-Username`: 사용자 이름
- `X-User-Roles`: 사용자 권한 목록 (쉼표로 구분)

## 아키텍처

### 패키지 구조
```
com.spartaecommerce.gateway
├── filter
│   └── auth
│       ├── strategy
│       │   ├── AuthenticationStrategy.java         # 인증 전략 인터페이스
│       │   ├── AuthenticationResult.java           # 인증 결과 DTO
│       │   ├── SessionAuthenticationStrategy.java  # 세션 인증 구현
│       │   └── JwtAuthenticationStrategy.java      # JWT 인증 구현 (향후)
│       ├── AccessLevel.java                        # 접근 레벨 enum
│       └── AuthenticationGatewayFilterFactory.java # 인증 필터
└── config
    ├── AuthenticationConfig.java                   # 인증 설정
    └── GatewayConfig.java                          # Gateway 설정
```

### 인증 플로우

```
1. 클라이언트 요청
   ↓
2. Gateway - AuthenticationGatewayFilterFactory
   ↓
3. Route metadata에서 access-level 추출
   ↓
4. AccessLevel 확인
   ├─ public → 인증 없이 통과
   └─ authenticated/admin → 인증 수행
      ↓
5. AuthenticationStrategy.authenticate()
   ├─ SessionAuthenticationStrategy (현재)
   │  └─ JSESSIONID 쿠키 확인
   └─ JwtAuthenticationStrategy (향후)
      └─ JWT 토큰 검증
      ↓
6. 권한 체크 (isAuthorized)
   ├─ 실패 → 401 Unauthorized
   └─ 성공 → 헤더에 사용자 정보 추가
      ↓
7. Downstream 서비스로 요청 전달
   ↓
8. 서비스에서 fine-grained 권한 체크
```

## 설정

### application.yml

```yaml
auth:
  # 인증 방식 선택: session (현재) 또는 jwt (향후)
  strategy: session

spring:
  cloud:
    gateway:
      # 모든 route에 인증 필터 자동 적용
      default-filters:
        - name: Authentication

      routes:
        - id: auth-public
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/auth/**
          metadata:
            access-level: public  # 접근 레벨 지정
```

## 향후 확장

### JWT 인증으로 전환하기

1. **JWT 라이브러리 추가** (build.gradle)
   ```gradle
   implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
   runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
   runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
   ```

2. **JwtAuthenticationStrategy 구현 완료**
   - JWT 파싱 및 검증 로직 추가
   - Claims에서 userId, username, roles 추출
   - 만료 시간 확인

3. **application.yml 설정 변경**
   ```yaml
   auth:
     strategy: jwt
     jwt:
       secret: ${JWT_SECRET}
       expiration: 3600000  # 1시간
   ```

4. **ecommerce-service JWT 발급 기능 추가**
   - 로그인 성공 시 JWT 토큰 생성
   - 리프레시 토큰 처리

### 추가 기능 구현 가능

- **Rate Limiting**: 요청 제한
- **Circuit Breaker**: 장애 격리
- **Request Logging**: 요청/응답 로깅
- **API Key 인증**: 외부 API용
- **OAuth2 통합**: 소셜 로그인

## 개발 및 테스트

### Gateway 실행
```bash
./gradlew :api-gateway:bootRun
```

Gateway는 8080 포트에서 실행됩니다.

### 테스트 예시

**Public 엔드포인트 (인증 불필요)**
```bash
# 상품 조회
curl http://localhost:8080/api/v1/products

# 회원가입
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"1234","email":"test@example.com"}'
```

**Authenticated 엔드포인트 (세션 필요)**
```bash
# 장바구니 조회 (JSESSIONID 쿠키 필요)
curl http://localhost:8080/api/v1/cart-items \
  -H "Cookie: JSESSIONID=xxxxx"
```

**Admin 엔드포인트 (관리자 권한 필요)**
```bash
# 상품 등록 (ADMIN 권한 필요)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Cookie: JSESSIONID=xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"name":"상품명","price":10000,"categoryId":1}'
```

## 모니터링

### Actuator 엔드포인트
- Health: http://localhost:8080/actuator/health
- Gateway Routes: http://localhost:8080/actuator/gateway/routes

### 로그 레벨
Gateway 관련 상세 로그 확인:
```yaml
logging:
  level:
    com.spartaecommerce.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```
