-- Closure Table 생성
CREATE TABLE IF NOT EXISTS category_closure
(
    ancestor_id   BIGINT NOT NULL,
    descendant_id BIGINT NOT NULL,
    depth         INT    NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id),
    CONSTRAINT fk_category_closure_ancestor FOREIGN KEY (ancestor_id) REFERENCES category (category_id),
    CONSTRAINT fk_category_closure_descendant FOREIGN KEY (descendant_id) REFERENCES category (category_id)
);

-- 인덱스 생성 (조회 성능 향상)
CREATE INDEX idx_descendant ON category_closure (descendant_id);
CREATE INDEX idx_depth ON category_closure (depth);

-- 기존 카테고리 데이터를 기반으로 Closure Table 초기화
-- 1. 각 카테고리와 자기 자신의 관계 (depth = 0)
INSERT INTO category_closure (ancestor_id, descendant_id, depth)
SELECT category_id, category_id, 0
FROM category
WHERE deleted = false;

-- 2. 부모-자식 관계 (depth = 1)
INSERT INTO category_closure (ancestor_id, descendant_id, depth)
SELECT c.parent_id, c.category_id, 1
FROM category c
WHERE c.parent_id IS NOT NULL
  AND c.deleted = false
  AND EXISTS(SELECT 1 FROM category p WHERE p.category_id = c.parent_id AND p.deleted = false);

-- 3. 조상-자손 관계 (depth >= 2, 재귀적으로 구성)
-- MySQL에서 재귀 CTE 사용 (MySQL 8.0+)
INSERT INTO category_closure (ancestor_id, descendant_id, depth)
WITH RECURSIVE category_tree AS (
    -- Base case: 직접 부모-자식 관계
    SELECT c.parent_id AS ancestor_id,
           c.category_id AS descendant_id,
           1 AS depth
    FROM category c
    WHERE c.parent_id IS NOT NULL
      AND c.deleted = false
      AND EXISTS(SELECT 1 FROM category p WHERE p.category_id = c.parent_id AND p.deleted = false)

    UNION ALL

    -- Recursive case: 조상의 모든 조상 찾기
    SELECT ct.ancestor_id,
           c.category_id,
           ct.depth + 1
    FROM category_tree ct
    INNER JOIN category c ON c.parent_id = ct.descendant_id
    WHERE c.deleted = false
      AND ct.depth < 10  -- 최대 깊이 제한 (무한 루프 방지)
)
SELECT DISTINCT ancestor_id, descendant_id, depth
FROM category_tree
WHERE NOT EXISTS (
    SELECT 1
    FROM category_closure cc
    WHERE cc.ancestor_id = category_tree.ancestor_id
      AND cc.descendant_id = category_tree.descendant_id
);
