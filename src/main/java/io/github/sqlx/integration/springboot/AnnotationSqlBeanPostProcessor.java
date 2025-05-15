package io.github.sqlx.integration.springboot;

import io.github.sqlx.jdbc.ConnectionWrapper;
import io.github.sqlx.jdbc.datasource.AdaptiveProxyDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;

/**
 * @author: he peng
 * @create: 2025/3/19 17:44
 * @description:
 */
public class AnnotationSqlBeanPostProcessor implements BeanPostProcessor {

    private final ConnectionWrapper connectionWrapper;

    public AnnotationSqlBeanPostProcessor(ConnectionWrapper connectionWrapper) {
        this.connectionWrapper = connectionWrapper;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (DataSource.class.isAssignableFrom(bean.getClass())) {
            return new AdaptiveProxyDataSource((DataSource) bean, connectionWrapper);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
