package com.github.sqlx.integration.jpa.dao;

import com.github.sqlx.integration.jpa.entity.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: he peng
 * @create: 2024/10/31 13:08
 * @description:
 */

@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, Long> {
}
