# Coupon Domain Documentation

## Overview
쿠폰/할인 도메인은 e-commerce 플랫폼에서 할인 쿠폰을 관리하고 적용하는 기능을 제공합니다.

## Table of Contents
- [Domain Rules](#domain-rules)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Database Design](#database-design)
- [Discount Calculation Rules](#discount-calculation-rules)
- [Concurrency Strategy](#concurrency-strategy)
- [Testing Strategy](#testing-strategy)

---

## Domain Rules

### Coupon Types
쿠폰은 두 가지 할인 타입을 지원합니다:

1. **PERCENT (할인율)**
   - `discountValue`: 1 ~ 100 (퍼센트)
   - `maxDiscountAmount`: 최대 할인 금액 (선택적, PERCENT 타입에서만 의미 있음)
   - 예: 10% 할인, 최대 5000원

2. **FIXED (정액 할인)**
   - `discountValue`: > 0 (원화)
   - `maxDiscountAmount`: 무시됨 (FIXED 타입에서는 의미 없음)
   - 예: 3000원 할인

### Active Coupon Definition
쿠폰이 **활성(active)** 상태가 되려면 다음 조건을 **모두** 만족해야 합니다:

1. `deleted = false` (논리 삭제되지 않음)
2. `startDate <= now <= endDate` (유효 기간 내)
3. `usedCount < usageLimit` (사용 가능 횟수 남음)

이 정의는 코드, 쿼리, 테스트에서 일관되게 적용됩니다.

### Validation Rules

#### Required Fields
- `couponName`: 1~100자, 중복 불가
- `discountType`: PERCENT 또는 FIXED
- `discountValue`: 타입별 유효 범위 검증
- `minOrderAmount`: >= 0
- `startDate`, `endDate`: startDate <= endDate
- `usageLimit`: > 0

#### Optional Fields
- `maxDiscountAmount`: PERCENT 타입일 때만 의미 있음, > 0 또는 null

### Soft Delete Policy
- `DELETE /api/v1/coupons/{couponId}`는 **논리 삭제**를 수행합니다.
- `deleted = true`, `deletedAt = timestamp` 설정
- 물리 삭제는 지양 (주문 이력, 감사 로그 참조 가능성)
- 논리 삭제된 쿠폰은 일반 조회에서 제외됨 (404 반환)

---

## Architecture

### Hexagonal Architecture (Ports & Adapters)

```
coupon/
├── domain/                     # 도메인 계층 (비즈니스 로직)
│   ├── entity/
│   │   ├── Coupon.java        # 도메인 엔티티 (Rich Domain Model)
│   │   └── DiscountType.java  # 할인 타입 enum
│   ├── repository/
│   │   └── CouponRepository.java  # 리포지토리 인터페이스 (포트)
│   └── service/
│       └── CouponDiscountCalculator.java  # 도메인 서비스
│
├── application/                # 애플리케이션 계층 (유스케이스)
│   ├── dto/
│   │   ├── command/           # 상태 변경 명령
│   │   ├── query/             # 조회 쿼리
│   │   └── result/            # 응답 결과
│   └── service/
│       └── CouponService.java # 애플리케이션 서비스 (트랜잭션 경계)
│
├── infrastructure/             # 인프라 계층 (어댑터)
│   └── persistence/jpa/
│       ├── entity/
│       │   └── CouponJpaEntity.java  # JPA 엔티티
│       └── repository/
│           ├── CouponJpaRepository.java  # Spring Data JPA
│           └── CouponRepositoryImpl.java # QueryDSL 구현
│
└── presentation/               # 프레젠테이션 계층
    └── controller/
        ├── CouponController.java
        └── dto/
            ├── request/
            └── response/
```

### Design Patterns

1. **Repository Pattern with Domain Separation**
   - 도메인 엔티티(`Coupon`)와 JPA 엔티티(`CouponJpaEntity`) 분리
   - `toDomain()`, `from()` 메서드로 변환

2. **Command/Query Separation**
   - Command: `CreateCouponCommand`, `UpdateCouponCommand`
   - Query: `CouponSearchQuery`
   - Result: `CouponResult`, `MaxDiscountResult`

3. **Domain Service**
   - `CouponDiscountCalculator`: 할인 계산 로직 캡슐화

---

## API Endpoints

### 1. POST /api/v1/coupons
쿠폰 생성

**Request:**
```json
{
  "couponName": "신규 회원 10% 할인",
  "discountType": "PERCENT",
  "discountValue": 10,
  "minOrderAmount": 10000,
  "maxDiscountAmount": 5000,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "usageLimit": 1000
}
```

**Response:** `201 Created`
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "couponId": 1
  }
}
```

### 2. GET /api/v1/coupons/{couponId}
쿠폰 단건 조회

**Response:** `200 OK`
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "couponId": 1,
    "couponName": "신규 회원 10% 할인",
    "discountType": "PERCENT",
    "discountValue": 10.00,
    "minOrderAmount": 10000.00,
    "maxDiscountAmount": 5000.00,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "usageLimit": 1000,
    "issuedCount": 0,
    "usedCount": 0,
    "deleted": false,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

**Error:** `404 Not Found` (논리 삭제된 쿠폰 포함)

### 3. GET /api/v1/coupons
쿠폰 목록 조회 (페이지네이션)

**Query Parameters:**
- `isActive` (optional): `true` (활성만) | `false` (비활성만) | null (전체)
- `page` (default: 0)
- `size` (default: 20)
- `sortBy` (default: "createdAt")
- `direction` (default: "DESC")

**Example:**
```
GET /api/v1/coupons?isActive=true&page=0&size=20
```

**Response:** `200 OK`
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

### 4. PATCH /api/v1/coupons/{couponId}
쿠폰 수정 (부분 업데이트)

**Request:**
```json
{
  "couponName": "새로운 이름",
  "endDate": "2025-12-31T23:59:59"
}
```

**Response:** `200 OK`

### 5. DELETE /api/v1/coupons/{couponId}
쿠폰 삭제 (논리 삭제)

**Response:** `200 OK`

**Note:** Idempotent (이미 삭제된 경우에도 200 반환)

### 6. GET /api/v1/products/{productId}/max-discount
상품별 최대 할인율 조회

**Business Rules:**
- 현재 시점에 **활성** 상태인 모든 쿠폰 대상
- 각 쿠폰의 할인율을 계산하여 최대값 반환
- PERCENT: 할인율 그대로 반환
- FIXED: `(discountValue / productPrice) * 100` 계산
- `maxDiscountAmount` 적용 후 환산

**Response:** `200 OK`
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "maxDiscountRate": 25.00,
    "couponId": 2,
    "couponName": "5000원 할인 쿠폰",
    "discountType": "FIXED",
    "discountValue": 5000.00,
    "calculatedDiscountAmount": 5000.00
  }
}
```

**Edge Cases:**
- 활성 쿠폰이 없는 경우: `maxDiscountRate = 0.00`, `couponId = null`
- 상품 가격이 0인 경우: `400 Bad Request`

---

## Database Design

### Table: `coupon`

#### Schema
```sql
CREATE TABLE coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    max_discount_amount DECIMAL(10, 2) NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    usage_limit INT NOT NULL DEFAULT 1,
    issued_count INT NOT NULL DEFAULT 0,
    used_count INT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,  -- Optimistic lock
    ...
);
```

### Indexes

#### 1. idx_coupon_active_search (deleted, start_date, end_date)
**Purpose:** 활성 쿠폰 조회 최적화

**Query Pattern:**
```sql
WHERE deleted = false
  AND start_date <= NOW()
  AND end_date >= NOW()
```

**Rationale:**
- `deleted`는 binary (true/false)로 선택도가 높음
- 날짜 범위 필터링을 위해 `start_date`, `end_date` 포함
- MySQL은 composite index를 왼쪽부터 사용 가능

#### 2. idx_coupon_name (coupon_name)
**Purpose:** 쿠폰 이름 기반 조회 및 중복 체크

**Use Cases:**
- `existsByName()`: 중복 이름 검증
- `findByName()`: 이름으로 조회

**Performance:** O(log n) 조회 성능

#### 3. idx_coupon_dates (start_date, end_date)
**Purpose:** 날짜 범위 쿼리 최적화

**Use Cases:**
- 관리자 페이지에서 유효 기간별 쿠폰 조회
- 통계 쿼리

### Index Strategy
- **Not Indexed:** `discount_type`, `discount_value`, `min_order_amount`
  - 이유: 낮은 카디널리티, 단독 검색 빈도 낮음
- **Covering Index 고려:** 자주 조회하는 컬럼만 SELECT할 경우 인덱스만으로 처리 가능

---

## Discount Calculation Rules

### PERCENT Type
```java
calculatedDiscount = orderAmount * (discountValue / 100)

if (maxDiscountAmount != null && calculatedDiscount > maxDiscountAmount) {
    actualDiscount = maxDiscountAmount
} else {
    actualDiscount = calculatedDiscount
}
```

**Example:**
- 주문 금액: 50,000원
- 할인율: 10%
- 최대 할인: 3,000원
- 계산: 50,000 * 0.10 = 5,000원 → **3,000원 적용** (최대 할인 적용)

### FIXED Type
```java
actualDiscount = min(discountValue, orderAmount)
```

**Example:**
- 주문 금액: 50,000원
- 할인 금액: 3,000원
- 계산: **3,000원 적용**

**Edge Case:**
- 주문 금액: 2,000원
- 할인 금액: 3,000원
- 계산: **2,000원 적용** (주문 금액을 초과할 수 없음)

### Discount Rate Conversion (for max-discount API)

#### PERCENT Type
```java
discountRate = discountValue  // 직접 사용
```

#### FIXED Type
```java
actualDiscountAmount = min(discountValue, productPrice)
discountRate = (actualDiscountAmount / productPrice) * 100
```

**Example:**
- 상품 가격: 20,000원
- FIXED 5,000원 → **25.00%**
- FIXED 30,000원 → **100.00%** (가격 초과)

---

## Concurrency Strategy

### Problem
`usedCount`, `issuedCount` 필드는 동시에 증가할 수 있습니다.

### Solution: Optimistic Locking

#### Implementation
```java
@Version
@Column(nullable = false)
private Long version;
```

#### JPA Entity
```java
@Entity
public class CouponJpaEntity {
    @Version
    private Long version;  // JPA가 자동으로 관리
}
```

#### How It Works
1. 엔티티 조회 시 현재 `version` 값 읽기
2. UPDATE 실행 시:
   ```sql
   UPDATE coupon
   SET used_count = used_count + 1,
       version = version + 1
   WHERE coupon_id = ? AND version = ?  -- 버전 일치 확인
   ```
3. 버전이 일치하지 않으면 `OptimisticLockException` 발생
4. 애플리케이션 레벨에서 재시도 또는 에러 처리

#### Why Not Pessimistic Lock?
- 쿠폰 사용은 read-heavy 작업 (조회 >> 사용)
- Pessimistic lock은 대기 시간 증가 → 성능 저하
- Optimistic lock은 충돌 빈도가 낮을 때 유리

#### Alternative: Atomic Update
```sql
UPDATE coupon
SET used_count = used_count + 1
WHERE coupon_id = ? AND used_count < usage_limit
```
- 애플리케이션 레벨에서 재조회 없이 DB 레벨에서 원자적 처리
- 향후 쿠폰 발급/사용 기능 추가 시 적용 가능

---

## Testing Strategy

### Principles (고전파 방식)
- **Mockito 최소화:** 실제 객체 또는 Fake 사용
- **리팩터링 내성:** 구현 세부사항이 아닌 동작 검증
- **실행 시간:** In-memory Fake repository로 빠른 실행

### Test Structure

#### 1. Domain Entity Test
**File:** `CouponTest.java`

**Coverage:**
- 생성 시 유효성 검증 (할인율 범위, 날짜 범위 등)
- `isActive()` 로직 (날짜, 사용 횟수, 삭제 상태)
- `canApplyToOrder()` 로직
- 경계 조건 (PERCENT 1~100, 날짜 경계)

#### 2. Domain Service Test
**File:** `CouponDiscountCalculatorTest.java`

**Coverage:**
- PERCENT 할인 계산 (최대 할인 적용/미적용)
- FIXED 할인 계산 (주문 금액 초과 케이스)
- 할인율 환산 (FIXED → %)
- 0원 상품 방어 로직

#### 3. Application Service Test (Integration)
**File:** `CouponServiceTest.java`

**Strategy:**
- **Fake Repository 사용** (In-memory ConcurrentHashMap)
- Real `CouponDiscountCalculator` 사용
- 트랜잭션 없이도 동작 검증 가능

**Coverage:**
- 쿠폰 생성/조회/수정/삭제
- 중복 이름 검증
- 활성/비활성 쿠폰 필터링
- 상품별 최대 할인율 계산 (여러 쿠폰 비교)

#### 4. Controller Test (Optional)
**File:** `CouponControllerTest.java`

**Strategy:**
- Spring MockMvc + REST Docs
- Service layer는 실제 사용 (E2E 테스트 성격)
- API 명세 자동 생성

### Fake Repository Benefits
1. **빠른 실행:** DB I/O 없음
2. **격리:** 다른 테스트와 독립적
3. **리팩터링 내성:** 쿼리 구현 변경해도 테스트 불변
4. **간단한 설정:** @SpringBootTest 불필요

---

## Future Enhancements

### 1. Product-Coupon Mapping
현재는 모든 쿠폰이 모든 상품에 적용 가능합니다. 향후 특정 상품/카테고리에만 적용 가능하도록 확장:

```sql
CREATE TABLE product_coupon_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    category_id BIGINT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id),
    UNIQUE KEY uk_coupon_product (coupon_id, product_id),
    UNIQUE KEY uk_coupon_category (coupon_id, category_id)
);
```

### 2. User-Coupon Issuance
사용자별 쿠폰 발급 이력 관리:

```sql
CREATE TABLE user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    issued_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    order_id BIGINT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id)
);
```

### 3. Coupon Usage History
주문별 쿠폰 사용 이력 (감사 로그):

```sql
CREATE TABLE coupon_usage_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    used_at DATETIME NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id)
);
```

---

## References
- [CLAUDE.md](../CLAUDE.md) - Project coding guidelines
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
