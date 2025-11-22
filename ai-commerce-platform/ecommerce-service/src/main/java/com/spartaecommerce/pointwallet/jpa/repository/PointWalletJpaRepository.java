package com.spartaecommerce.pointwallet.jpa.repository;

import com.spartaecommerce.pointwallet.jpa.entity.PointWalletJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointWalletJpaRepository extends JpaRepository<PointWalletJpaEntity, Long> {

    Optional<PointWalletJpaEntity> findByUserId(Long userId);
}