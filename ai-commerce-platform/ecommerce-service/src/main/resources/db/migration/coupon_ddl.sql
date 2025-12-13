-- Coupon Table DDL for MySQL
-- Author: Claude Code
-- Description: Coupon/Discount domain table with optimized indexes

CREATE TABLE IF NOT EXISTS coupon (
    -- Primary Key
    coupon_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- Coupon Information
    coupon_name VARCHAR(100) NOT NULL COMMENT '쿠폰 이름',
    discount_type VARCHAR(20) NOT NULL COMMENT '할인 타입: PERCENT, FIXED',
    discount_value DECIMAL(10, 2) NOT NULL COMMENT '할인 값 (PERCENT: 1-100, FIXED: 금액)',
    min_order_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '최소 주문 금액',
    max_discount_amount DECIMAL(10, 2) NULL COMMENT '최대 할인 금액 (PERCENT 타입에서만 의미있음)',

    -- Scope (적용 범위)
    scope_type VARCHAR(20) NOT NULL COMMENT '적용 범위: ALL(전체), CATEGORY(카테고리), PRODUCT(상품)',
    scope_id BIGINT NULL COMMENT '범위 ID (ALL: null, CATEGORY: categoryId, PRODUCT: productId)',

    -- Date Range
    start_date DATETIME NOT NULL COMMENT '쿠폰 유효 시작일',
    end_date DATETIME NOT NULL COMMENT '쿠폰 유효 종료일',

    -- Usage Tracking
    usage_limit INT NOT NULL DEFAULT 1 COMMENT '총 사용 가능 횟수',
    issued_count INT NOT NULL DEFAULT 0 COMMENT '발급된 횟수',
    used_count INT NOT NULL DEFAULT 0 COMMENT '사용된 횟수',

    -- Soft Delete
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '논리 삭제 여부',
    deleted_at DATETIME NULL COMMENT '삭제 일시',

    -- Audit Fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    -- Optimistic Lock
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',

    -- Constraints
    CONSTRAINT chk_coupon_discount_type
        CHECK (discount_type IN ('PERCENT', 'FIXED')),

    CONSTRAINT chk_coupon_scope_type
        CHECK (scope_type IN ('ALL', 'CATEGORY', 'PRODUCT')),

    CONSTRAINT chk_coupon_discount_value_positive
        CHECK (discount_value > 0),

    CONSTRAINT chk_coupon_min_order_amount_non_negative
        CHECK (min_order_amount >= 0),

    CONSTRAINT chk_coupon_max_discount_amount_positive
        CHECK (max_discount_amount IS NULL OR max_discount_amount > 0),

    CONSTRAINT chk_coupon_date_range
        CHECK (start_date <= end_date),

    CONSTRAINT chk_coupon_usage_limit_positive
        CHECK (usage_limit > 0),

    CONSTRAINT chk_coupon_issued_count_non_negative
        CHECK (issued_count >= 0),

    CONSTRAINT chk_coupon_used_count_non_negative
        CHECK (used_count >= 0),

    CONSTRAINT chk_coupon_used_count_within_limit
        CHECK (used_count <= usage_limit)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='쿠폰 테이블';

-- ====================================================================
-- INDEXES
-- ====================================================================

-- Index for active coupon search
-- Purpose: Optimize queries that filter by deleted status and date range
-- Used by: findAllActiveCoupons(), search() with isActive=true
-- Rationale: Composite index (deleted, start_date, end_date) allows efficient
--            filtering of active coupons using index-only access
CREATE INDEX idx_coupon_active_search
    ON coupon(deleted, start_date, end_date)
    COMMENT 'Optimizes active coupon filtering by deleted status and date range';

-- Index for coupon name lookup
-- Purpose: Support unique name validation and name-based searches
-- Used by: existsByName(), findByName()
-- Rationale: Coupon names are frequently queried for duplicate checks during creation
CREATE INDEX idx_coupon_name
    ON coupon(coupon_name)
    COMMENT 'Supports name-based lookups and duplicate checks';

-- Index for date range queries
-- Purpose: Optimize queries filtering by start/end dates
-- Used by: search() queries with date filters
-- Rationale: Supports efficient range scans for coupon validity periods
CREATE INDEX idx_coupon_dates
    ON coupon(start_date, end_date)
    COMMENT 'Optimizes date range filtering queries';

-- Index for scope-based queries
-- Purpose: Optimize queries filtering by scope (ALL, CATEGORY, PRODUCT)
-- Used by: findApplicableCouponsForProduct()
-- Rationale: Enables efficient lookups for product-specific coupon filtering
CREATE INDEX idx_coupon_scope
    ON coupon(scope_type, scope_id)
    COMMENT 'Optimizes scope-based coupon filtering (ALL/CATEGORY/PRODUCT)';

-- ====================================================================
-- INDEX STRATEGY EXPLANATION
-- ====================================================================
--
-- 1. idx_coupon_active_search (deleted, start_date, end_date):
--    - Most selective filter first (deleted is binary: true/false)
--    - Date range follows for active coupon determination
--    - Covers the common query pattern: WHERE deleted=false AND start_date <= NOW() AND end_date >= NOW()
--    - MySQL can use this for index-only scans in many cases
--
-- 2. idx_coupon_name:
--    - Single column index for exact name matches
--    - Essential for O(log n) duplicate name checks
--    - Can be extended to unique index if business rules require strict uniqueness
--
-- 3. idx_coupon_dates:
--    - Supports queries that filter by date ranges
--    - Useful for admin queries showing coupons by validity period
--    - Can leverage range scans efficiently
--
-- Not indexed:
-- - discount_type, discount_value, min_order_amount: Low cardinality, rarely queried alone
-- - usage_limit, issued_count, used_count: Typically used in WHERE clauses with other filters
-- - version: Only used for optimistic locking, not for searching
-- ====================================================================

-- ====================================================================
-- SAMPLE DATA (for testing/development)
-- ====================================================================

-- Uncomment to insert sample data
/*
INSERT INTO coupon (
    coupon_name, discount_type, discount_value, min_order_amount,
    max_discount_amount, scope_type, scope_id, start_date, end_date, usage_limit
) VALUES
(
    '신규 회원 10% 할인',
    'PERCENT',
    10.00,
    10000.00,
    5000.00,
    'ALL',
    NULL,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1000
),
(
    '3000원 즉시 할인',
    'FIXED',
    3000.00,
    15000.00,
    NULL,
    'ALL',
    NULL,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    500
),
(
    'VIP 20% 할인',
    'PERCENT',
    20.00,
    50000.00,
    10000.00,
    'ALL',
    NULL,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 60 DAY),
    100
),
(
    '전자제품 카테고리 15% 할인',
    'PERCENT',
    15.00,
    20000.00,
    8000.00,
    'CATEGORY',
    1, -- categoryId 1 (예시)
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    500
),
(
    '특정 상품 5000원 할인',
    'FIXED',
    5000.00,
    30000.00,
    NULL,
    'PRODUCT',
    100, -- productId 100 (예시)
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 14 DAY),
    200
);
*/
