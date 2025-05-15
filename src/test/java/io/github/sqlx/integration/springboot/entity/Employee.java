package io.github.sqlx.integration.springboot.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * employee entity
 *
 * @author jing yun
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class Employee {

    private Long id;

    private String name;

    private Long departmentId;
}
