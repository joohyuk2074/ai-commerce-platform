package com.spartaecommerce.category.adapter.out.persistence;

import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryClosureJpaEntity;
import com.spartaecommerce.category.adapter.out.persistence.jpa.entity.CategoryJpaEntity;
import com.spartaecommerce.category.adapter.out.persistence.jpa.repository.CategoryClosureJpaRepository;
import com.spartaecommerce.category.adapter.out.persistence.jpa.repository.CategoryJpaRepository;
import com.spartaecommerce.category.domain.port.out.CategoryClosurePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryClosurePersistenceAdapter implements CategoryClosurePort {

    private final CategoryClosureJpaRepository categoryClosureJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public void insertClosureForNewCategory(Long categoryId, Long parentCategoryId) {
        List<CategoryClosureJpaEntity> closureEntries = new ArrayList<>();

        // 1. 자기 자신과의 관계 (depth = 0)
        closureEntries.add(CategoryClosureJpaEntity.of(categoryId, categoryId, 0));

        // 2. 부모가 있는 경우, 부모의 모든 조상과의 관계 추가
        if (parentCategoryId != null) {
            List<CategoryClosureJpaEntity> parentClosures =
                categoryClosureJpaRepository.findAllByDescendantId(parentCategoryId);

            for (CategoryClosureJpaEntity parentClosure : parentClosures) {
                CategoryClosureJpaEntity ancestor = CategoryClosureJpaEntity.of(
                    parentClosure.getAncestorId(),
                    categoryId,
                    parentClosure.getDepth() + 1
                );
                closureEntries.add(ancestor);
            }
        }

        categoryClosureJpaRepository.saveAll(closureEntries);
        log.info("Created {} closure entries for category {}", closureEntries.size(), categoryId);
    }

    @Override
    public void deleteClosureForCategory(Long categoryId) {
        // 해당 카테고리가 조상이거나 자손인 모든 관계 삭제
        categoryClosureJpaRepository.deleteAllByDescendantId(categoryId);
        categoryClosureJpaRepository.deleteAllByAncestorId(categoryId);
        log.info("Deleted all closure entries for category {}", categoryId);
    }

    @Override
    public void rebuildClosureTable() {
        log.info("Starting closure table rebuild...");

        // 1. 기존 Closure Table 전체 삭제
        categoryClosureJpaRepository.deleteAll();

        // 2. 모든 카테고리 조회
        List<CategoryJpaEntity> allCategories = categoryJpaRepository.findAll();

        // 3. 각 카테고리에 대해 Closure 엔트리 재생성
        List<CategoryClosureJpaEntity> allClosureEntries = new ArrayList<>();

        for (CategoryJpaEntity category : allCategories) {
            if (category.isDeleted()) {
                continue;
            }

            // 자기 자신과의 관계
            allClosureEntries.add(CategoryClosureJpaEntity.of(
                category.getCategoryId(),
                category.getCategoryId(),
                0
            ));

            // 부모 계층 추적
            CategoryJpaEntity current = category;
            int depth = 1;

            while (current.getParent() != null && !current.getParent().isDeleted()) {
                CategoryJpaEntity parent = current.getParent();
                allClosureEntries.add(CategoryClosureJpaEntity.of(
                    parent.getCategoryId(),
                    category.getCategoryId(),
                    depth
                ));

                current = parent;
                depth++;
            }
        }

        // 4. 일괄 저장
        categoryClosureJpaRepository.saveAll(allClosureEntries);
        log.info("Closure table rebuild completed. Total entries: {}", allClosureEntries.size());
    }
}
