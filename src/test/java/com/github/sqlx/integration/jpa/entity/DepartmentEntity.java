package com.github.sqlx.integration.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author: he peng
 * @create: 2024/10/31 13:05
 * @description:
 */

@Entity
@Table(name = "department")
@Data
public class DepartmentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private Long areaId;
}
