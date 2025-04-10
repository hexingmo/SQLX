package com.github.sqlx.config;

import com.github.sqlx.exception.ConfigurationException;
import com.github.sqlx.metrics.MetricsCollectMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MetricsConfiguration}.
 * This class tests the configuration and validation of metrics settings.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class MetricsConfigurationTest {

    private MetricsConfiguration metricsConfig;

    @BeforeEach
    void setUp() {
        metricsConfig = new MetricsConfiguration();
        metricsConfig.setEnabled(true);
        metricsConfig.setUsername("admin");
        metricsConfig.setPassword("password");
        metricsConfig.setSlowSqlMillis(1000L);
        metricsConfig.setSlowTransactionMillis(2000L);
        metricsConfig.setFileDirectory("/var/logs/sqlx");
        metricsConfig.setCollectScope(MetricsCollectScope.SLOW);
        metricsConfig.setCollectMode(MetricsCollectMode.SYNC);
        metricsConfig.setDataRetentionDuration(Duration.ofDays(3));
        metricsConfig.setCollectKeepAliveMillis(3000L);
        metricsConfig.setCollectQueueCapacity(4000);
        metricsConfig.setCollectCorePoolSize(10);
        metricsConfig.setCollectMaxPoolSize(20);
    }

    @Test
    void testValidateSuccess() {
        assertDoesNotThrow(() -> metricsConfig.validate());
    }

    @Test
    void testValidateMissingUsername() {
        metricsConfig.setUsername(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("metrics [username] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateMissingPassword() {
        metricsConfig.setPassword(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("metrics [password] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateMissingFileDirectory() {
        metricsConfig.setFileDirectory(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("metrics [fileDirectory] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateSlowScopeWithoutSlowSqlMillis() {
        metricsConfig.setSlowSqlMillis(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectScope is SLOW metrics [slowSqlMillis] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateSlowScopeWithoutSlowTransactionMillis() {
        metricsConfig.setSlowTransactionMillis(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectScope is SLOW metrics [slowTransactionMillis] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateAsyncModeWithoutCorePoolSize() {
        metricsConfig.setCollectMode(MetricsCollectMode.ASYNC);
        metricsConfig.setCollectCorePoolSize(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectMode is ASYNC metrics [collectCorePoolSize] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateAsyncModeWithoutMaxPoolSize() {
        metricsConfig.setCollectMode(MetricsCollectMode.ASYNC);
        metricsConfig.setCollectMaxPoolSize(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectMode is ASYNC metrics [collectMaxPoolSize] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateAsyncModeWithoutKeepAliveMillis() {
        metricsConfig.setCollectMode(MetricsCollectMode.ASYNC);
        metricsConfig.setCollectKeepAliveMillis(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectMode is ASYNC metrics [collectKeepAliveMillis] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateAsyncModeWithoutQueueCapacity() {
        metricsConfig.setCollectMode(MetricsCollectMode.ASYNC);
        metricsConfig.setCollectQueueCapacity(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> metricsConfig.validate());
        assertEquals("when collectMode is ASYNC metrics [collectQueueCapacity] attr must not be empty", exception.getMessage());
    }
}