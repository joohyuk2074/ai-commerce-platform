# Order Service MSA 분리 완료 문서

## 📋 개요

ecommerce-service에서 Order 도메인을 별도의 **order-service**로 MSA 패턴에 따라 분리 완료했습니다.

### 서비스 구성

```
ai-commerce-platform/
├── common/                  # 공통 도메인 객체 및 유틸리티
├── api-gateway/            # API Gateway (Port 8080)
├── auth-service/           # 인증 서비스 (Port 8083)
├── ecommerce-service/      # 전자상거래 서비스 (Port 8082) - Product, Category, PointWallet 관리
├── order-service/          # 주문 서비스 (Port 8084) ✨ NEW
└── chat-service/           # 채팅 서비스
```

---

## 🎯 주요 변경사항

### 1. order-service 모듈 생성

**기본 설정**
- Port: `8084`
- Database: `sparta_order_db`
- 헥사고날 아키텍처 유지
- WebClient 기반 외부 서비스 호출

**디렉토리 구조**
```
order-service/
├── src/main/java/com/spartaecommerce/
│   ├── OrderServiceApplication.java
│   ├── config/
│   │   └── WebClientConfig.java
│   ├── adapter/
│   │   ├── in/web/                    # REST Controllers
│   │   └── out/
│   │       ├── persistence/           # JPA Repositories
│   │       ├── event/                 # Kafka Event Publisher
│   │       └── external/              # 외부 서비스 클라이언트 ✨
│   │           ├── EcommerceServiceClient.java
│   │           └── dto/
│   └── order/
│       ├── domain/                    # Order 도메인 모델
│       │   ├── entity/
│       │   ├── port/
│       │   └── event/
│       └── application/               # Order 유스케이스
│           ├── OrderCommandService.java
│           ├── OrderItemProcessor.java     # ✨ MSA 버전
│           └── OrderPointProcessor.java    # ✨ MSA 버전
└── src/main/resources/
    └── application.yml
```

---

### 2. 공통 도메인 객체 이동

**common 모듈로 이동**
```
common/src/main/java/com/spartaecommerce/common/domain/
├── product/
│   ├── Product.java               # 상품 도메인 객체
│   └── ExternalProductRef.java
├── category/
│   └── Category.java              # 카테고리 도메인 객체
└── pointwallet/
    ├── PointWallet.java           # 포인트 지갑 도메인 객체
    ├── PointTransaction.java
    ├── PointTransactionType.java
    └── PointPolicy.java
```

**장점**
- 서비스 간 도메인 객체 공유
- 중복 코드 제거
- 일관성 유지

---

### 3. ecommerce-service Internal API 추가

외부 서비스(order-service)에서 호출할 수 있는 Internal API 구현

#### 3.1 Product API

```java
// ProductInternalController.java
POST   /internal/v1/products/bulk              # 상품 조회 (Bulk)
POST   /internal/v1/products/stocks/deduct     # 재고 차감
POST   /internal/v1/products/stocks/restore    # 재고 복구
```

**Request/Response**
```json
// Bulk 조회 Request
["productId1", "productId2", ...]

// Bulk 조회 Response
{
  "code": "OK",
  "message": "Success",
  "data": [
    {
      "productId": 1,
      "name": "상품명",
      "price": 10000,
      "stock": 100,
      "categoryId": 1,
      "isOrderable": true
    }
  ]
}

// 재고 차감/복구 Request
[
  {"productId": 1, "quantity": 2},
  {"productId": 2, "quantity": 1}
]
```

#### 3.2 Category API

```java
// CategoryInternalController.java
POST   /internal/v1/categories/bulk            # 카테고리 조회 (Bulk)
```

#### 3.3 PointWallet API

```java
// PointWalletInternalController.java
GET    /internal/v1/point-wallets/users/{userId}           # 포인트 지갑 조회
POST   /internal/v1/point-wallets/users/{userId}/use      # 포인트 사용
POST   /internal/v1/point-wallets/users/{userId}/earn     # 포인트 적립
POST   /internal/v1/point-wallets/calculate-points        # 포인트 계산
```

**Request/Response**
```json
// 포인트 사용/적립 Request
{
  "amount": 1000,
  "description": "주문 사용 (Order ID: 123)"
}

// 포인트 계산 Request
{
  "orderItems": [
    {
      "productId": 1,
      "categoryId": 1,
      "totalPrice": 10000
    }
  ],
  "userGrade": "VIP"
}

// 포인트 계산 Response
{
  "code": "OK",
  "data": {
    "expectedPoints": 500
  }
}
```

---

### 4. WebClient 기반 서비스 간 통신

#### 4.1 EcommerceServiceClient

**목적**: order-service에서 ecommerce-service의 Internal API 호출

```java
@Component
public class EcommerceServiceClient {
    private final WebClient ecommerceServiceWebClient;

    // Product 관련
    public List<Product> getProducts(List<Long> productIds);
    public void deductStocks(Map<Long, Integer> productIdToQuantity);
    public void restoreStocks(Map<Long, Integer> productIdToQuantity);

    // Category 관련
    public List<Category> getCategories(List<Long> categoryIds);

    // PointWallet 관련
    public PointWallet getPointWallet(Long userId);
    public void usePoints(Long userId, BigDecimal amount, String description);
    public void earnPoints(Long userId, BigDecimal amount, String description);
    public BigDecimal calculatePoints(List<OrderItemForCalculation> orderItems, String userGrade);
}
```

**설정** (order-service/application.yml)
```yaml
external:
  ecommerce-service:
    url: http://localhost:8082
    timeout: 5000
```

#### 4.2 통신 흐름

```
order-service (Port 8084)
    │
    │ HTTP (WebClient)
    ↓
ecommerce-service (Port 8082)
    ├── /internal/v1/products/*
    ├── /internal/v1/categories/*
    └── /internal/v1/point-wallets/*
```

---

### 5. Application 레이어 MSA 변환

#### 5.1 OrderItemProcessor (Before → After)

**Before** (Monolithic)
```java
@Service
public class OrderItemProcessor {
    private final LoadProductPort loadProductPort;
    private final LoadCategoryPort loadCategoryPort;

    public List<Product> loadProducts(...) {
        return loadProductPort.findAllByProductIdIn(productIds);
    }

    public void deductStocks(...) {
        product.deductQuantity(requestedQuantity);  // 직접 도메인 변경
    }
}
```

**After** (MSA)
```java
@Service
public class OrderItemProcessor {
    private final EcommerceServiceClient ecommerceServiceClient;  // ✨

    public List<Product> loadProducts(...) {
        return ecommerceServiceClient.getProducts(productIds);  // ✨ 외부 API 호출
    }

    public void deductStocks(...) {
        product.deductQuantity(requestedQuantity);  // 검증만
        ecommerceServiceClient.deductStocks(quantities);  // ✨ 실제 차감은 외부 서비스
    }
}
```

#### 5.2 OrderPointProcessor (Before → After)

**Before** (Monolithic)
```java
@Service
public class OrderPointProcessor {
    private final PointCalculator pointCalculator;
    private final LoadPointWalletPort loadPointWalletPort;
    private final SavePointWalletPort savePointWalletPort;

    public Money usePoints(Long userId, BigDecimal amount) {
        PointWallet wallet = loadPointWalletPort.getByUserId(userId);
        wallet.usePoints(amount);
        savePointWalletPort.save(wallet);  // 직접 저장
        return Money.from(amount);
    }
}
```

**After** (MSA)
```java
@Service
public class OrderPointProcessor {
    private final EcommerceServiceClient ecommerceServiceClient;  // ✨

    public Money usePoints(Long userId, BigDecimal amount, Long orderId) {
        ecommerceServiceClient.usePoints(
            userId,
            amount,
            "주문 사용 (Order ID: " + orderId + ")"
        );  // ✨ 외부 API 호출
        return Money.from(amount);
    }
}
```

---

### 6. 주문 생성 흐름 비교

#### Before (Monolithic)

```
OrderCommandService.create()
    │
    ├─ LoadProductPort.findAll()           # DB 직접 조회
    ├─ Product.deductQuantity()            # 도메인 변경
    ├─ SaveProductPort.saveAll()           # DB 직접 저장
    │
    ├─ LoadPointWalletPort.getByUserId()   # DB 직접 조회
    ├─ PointWallet.usePoints()             # 도메인 변경
    └─ SavePointWalletPort.save()          # DB 직접 저장
```

#### After (MSA)

```
order-service: OrderCommandService.create()
    │
    ├─ EcommerceServiceClient.getProducts()
    │       │
    │       └─→ ecommerce-service: ProductInternalController.getProducts()
    │                   └─ LoadProductPort.findAll()  # 자신의 DB 조회
    │
    ├─ EcommerceServiceClient.deductStocks()
    │       │
    │       └─→ ecommerce-service: ProductInternalController.deductStocks()
    │                   ├─ Product.deductQuantity()
    │                   └─ SaveProductPort.saveAll()  # 자신의 DB 저장
    │
    ├─ EcommerceServiceClient.usePoints()
    │       │
    │       └─→ ecommerce-service: PointWalletInternalController.usePoints()
    │                   ├─ PointWallet.usePoints()
    │                   └─ SavePointWalletPort.save()  # 자신의 DB 저장
    │
    └─ SaveOrderPort.save()  # order-service 자신의 DB에만 저장
```

**핵심 차이점**
- ✅ 각 서비스가 자신의 데이터베이스만 관리
- ✅ HTTP API를 통한 서비스 간 통신
- ✅ 명확한 경계 및 책임 분리

---

## 🔧 기술 스택

### order-service
- Spring Boot 3.x
- Spring Web (REST API)
- Spring WebFlux (WebClient)
- Spring Data JPA + QueryDSL
- MySQL (sparta_order_db)
- Redisson (분산 락)
- Kafka (이벤트 발행)

### 서비스 간 통신
- **동기 통신**: WebClient (HTTP/REST)
- **비동기 통신**: Kafka (이벤트 기반)

---

## 📊 데이터베이스 분리

### Before (Monolithic)
```
sparta_ecommerce
├── orders                # Order 테이블
├── order_items
├── order_status_history
├── products             # Product 테이블
├── categories           # Category 테이블
├── point_wallets        # PointWallet 테이블
└── point_transactions
```

### After (MSA)

**sparta_order_db** (order-service)
```
sparta_order_db
├── orders                # Order 테이블
├── order_items
└── order_status_history
```

**sparta_ecommerce** (ecommerce-service)
```
sparta_ecommerce
├── products             # Product 테이블
├── categories           # Category 테이블
├── point_wallets        # PointWallet 테이블
├── point_transactions
├── coupons
└── cart
```

---

## 🚀 실행 방법

### 1. 데이터베이스 생성
```sql
CREATE DATABASE sparta_order_db;
CREATE DATABASE sparta_ecommerce;
```

### 2. 서비스 실행 순서
```bash
# 1. ecommerce-service 실행 (Port 8082)
./gradlew :ecommerce-service:bootRun

# 2. order-service 실행 (Port 8084)
./gradlew :order-service:bootRun

# 3. auth-service 실행 (Port 8083)
./gradlew :auth-service:bootRun

# 4. api-gateway 실행 (Port 8080)
./gradlew :api-gateway:bootRun
```

### 3. 주문 API 호출
```bash
# API Gateway를 통한 호출
POST http://localhost:8080/api/v1/orders

# 직접 order-service 호출
POST http://localhost:8084/api/v1/orders
```

---

## 🎯 MSA 패턴 적용

### 1. Database per Service
- 각 서비스가 독립적인 데이터베이스 소유
- order-service: sparta_order_db
- ecommerce-service: sparta_ecommerce

### 2. API Composition
- order-service가 여러 서비스의 데이터를 조합
- WebClient를 통한 동기식 조합

### 3. Anti-Corruption Layer
- EcommerceServiceClient가 ACL 역할
- 외부 서비스의 변경으로부터 order-service 보호

### 4. Shared Kernel (제한적 사용)
- common 모듈로 도메인 객체 공유
- 초기 MSA 도입 시 실용적 접근

---

## ⚠️ 향후 개선 사항

### 1. 비동기 처리 (Saga Pattern)
현재: 동기식 WebClient 호출
```java
// 현재
ecommerceServiceClient.deductStocks(quantities);  // 동기
ecommerceServiceClient.usePoints(userId, amount); // 동기
```

개선: Saga 패턴 + 이벤트 기반
```java
// 개선안
orderEventPublisher.publishOrderCreated(order);  // 비동기
// → ecommerce-service가 이벤트 구독하여 재고 차감
// → rollback 시 보상 트랜잭션 실행
```

### 2. Circuit Breaker
- Resilience4j 추가
- 서비스 장애 시 fallback 처리

### 3. Service Mesh
- Istio/Linkerd 도입
- 서비스 간 통신 관리

### 4. 도메인 객체 완전 분리
- common 모듈 의존성 제거
- 각 서비스가 자신의 도메인 객체만 관리

---

## 📈 성능 고려사항

### 1. 네트워크 오버헤드
- **문제**: WebClient 호출로 인한 지연
- **해결**:
  - 비동기 처리 도입
  - 캐싱 전략
  - Batch API 설계

### 2. 트랜잭션 경계
- **문제**: 분산 트랜잭션 불가
- **해결**:
  - Saga 패턴
  - 최종 일관성(Eventual Consistency)

### 3. 데이터 중복
- **문제**: 여러 서비스에 데이터 복제 필요
- **해결**:
  - CQRS 패턴
  - 이벤트 소싱

---

## ✅ 체크리스트

- [x] order-service 모듈 생성
- [x] 도메인 객체 common 모듈로 이동
- [x] ecommerce-service Internal API 구현
- [x] WebClient 기반 EcommerceServiceClient 구현
- [x] OrderItemProcessor MSA 버전 변환
- [x] OrderPointProcessor MSA 버전 변환
- [x] OrderCommandService 수정
- [x] 전체 프로젝트 빌드 성공
- [ ] 통합 테스트 작성
- [ ] API 문서 업데이트
- [ ] 모니터링 설정

---

## 📚 참고 자료

- [Microservices Pattern](https://microservices.io/patterns/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

---

**작성일**: 2026-01-21
**작성자**: Claude Code
**버전**: 1.0
