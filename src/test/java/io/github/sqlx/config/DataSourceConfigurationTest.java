package io.github.sqlx.config;

import io.github.sqlx.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DataSourceConfiguration}.
 * @author He Xing Mo
 * @since 1.0
 */
class DataSourceConfigurationTest {

    private DataSourceConfiguration config;

    @BeforeEach
    void setUp() {
        config = new DataSourceConfiguration();
        Map<String, String> props = new HashMap<>();
        props.put("driverClassName", "org.h2.Driver");
        props.put("url", "jdbc:h2:mem:testdb");
        props.put("username", "sa");
        props.put("password", "sa");
        config.setProps(props);
        config.setName("TestDataSource");
        config.setDataSourceClass("org.h2.jdbcx.JdbcDataSource");
        config.setDestroyMethod("");
    }

    @Test
    void testValidateSuccess() {
        assertDoesNotThrow(() -> config.validate());
    }

    @Test
    void testValidateMissingName() {
        config.setName(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [name] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateMissingDataSourceClass() {
        config.setDataSourceClass(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [dataSourceClass] attr must not be null", exception.getMessage());
    }

    @Test
    void testValidateMissingProps() {
        config.setProps(null);
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [props] attr must not be empty", exception.getMessage());
    }

    @Test
    void testValidateMissingDriverClassName() {
        config.getProps().remove("driverClassName");
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [props] attr must contain driverClassName", exception.getMessage());
    }

    @Test
    void testValidateMissingJdbcUrl() {
        config.getProps().remove("url");
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [props] attr must contain jdbcUrl", exception.getMessage());
    }

    @Test
    void testValidateMissingUsername() {
        config.getProps().remove("username");
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [props] attr must contain username", exception.getMessage());
    }

    @Test
    void testValidateMissingPassword() {
        config.getProps().remove("password");
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> config.validate());
        assertEquals("dataSources [props] attr must contain password", exception.getMessage());
    }

    @Test
    void testAddProperty() {
        config.addProperty("newKey", "newValue");
        assertEquals("newValue", config.getProps().get("newKey"));
    }

    @Test
    void testRemoveProperty() {
        config.addProperty("removeKey", "removeValue");
        String removedValue = config.removeProperty("removeKey");
        assertEquals("removeValue", removedValue);
        assertNull(config.getProps().get("removeKey"));
    }

    @Test
    void testGetDriverClass() {
        assertEquals("org.h2.Driver", config.getDriverClass());
    }

    @Test
    void testGetJdbcUrl() {
        assertEquals("jdbc:h2:mem:testdb", config.getJdbcUrl());
    }

    @Test
    void testGetUsername() {
        assertEquals("sa", config.getUsername());
    }

    @Test
    void testGetPassword() {
        assertEquals("sa", config.getPassword());
    }

    @Test
    void testGetNodeAttribute() {
        assertNotNull(config.getNodeAttribute());
    }

    @Test
    void testEqualsAndHashCode() {
        DataSourceConfiguration anotherConfig = new DataSourceConfiguration();
        anotherConfig.setName("TestDataSource");
        assertEquals(config, anotherConfig);
        assertEquals(config.hashCode(), anotherConfig.hashCode());
    }
}
