package io.github.sqlx.integration.springboot;

import io.github.sqlx.integration.springboot.properties.SqlXProperties;
import io.github.sqlx.jdbc.AnnotationSqlConnectionWrapper;
import io.github.sqlx.sql.parser.DefaultAnnotationSqlHintParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 *
 * @author He Xing Mo
 * @since 1.0
 */
@EnableConfigurationProperties(SqlXProperties.class)
@ConditionalOnProperty(prefix = "sqlx" , name = "enabled" , havingValue = "false")
@Slf4j
public class SqlXDisableAutoConfiguration {

    @Bean
    public AnnotationSqlBeanPostProcessor annotationSqlBeanPostProcessor() {
        return new AnnotationSqlBeanPostProcessor(new AnnotationSqlConnectionWrapper(new DefaultAnnotationSqlHintParser()));
    }
}
