-- Migration: Add version column to coupon_user table for optimistic locking
-- Author: Claude Code
-- Date: 2025-12-13
-- Description: 동시성 제어를 위한 낙관적 락 버전 컬럼 추가

-- Step 1: Add version column to coupon_user table
ALTER TABLE coupon_user
ADD COLUMN version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전';

-- Step 2: Verify code column has unique constraint (should already exist from JPA entity)
-- The unique constraint is already defined in the JPA entity:
-- @Column(name = "code", nullable = false, unique = true, length = 20)
-- @Index(name = "idx_coupon_user_code", columnList = "code", unique = true)
--
-- If the table was created manually without JPA, uncomment the following:
-- ALTER TABLE coupon_user
-- ADD CONSTRAINT uk_coupon_user_code UNIQUE (code);
