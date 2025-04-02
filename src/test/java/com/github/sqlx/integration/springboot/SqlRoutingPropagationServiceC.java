package com.github.sqlx.integration.springboot;

import com.github.sqlx.annotation.SqlRouting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: he peng
 * @create: 2024/10/29 11:41
 * @description:
 */

@Service
@Slf4j
public class SqlRoutingPropagationServiceC {

    @SqlRouting(cluster = "cluster_0" , nodes = {"write_0" , "read_0"})
    public void propagation() {
        log.info("propagation run ...");
    }

    @SqlRouting(cluster = "cluster_0" , nodes = {"read_0"} , propagation = false)
    public void notPropagation() {

        log.info("notPropagation run ...");
    }
}
