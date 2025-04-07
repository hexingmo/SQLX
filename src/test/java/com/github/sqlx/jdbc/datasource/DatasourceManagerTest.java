package com.github.sqlx.jdbc.datasource;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.NodeState;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.exception.ManagementException;
import com.github.sqlx.exception.NoSuchDataSourceException;
import com.github.sqlx.exception.SqlXRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DatasourceManager}.
 * This class tests the management logic of DatasourceManager.
 * 
 * Author: He Xing Mo
 * Version: 1.0
 */
class DatasourceManagerTest {

    private SqlXConfiguration configuration;
    private DatasourceManager datasourceManager;
    private DataSourceWrapper dataSourceWrapper;
    private NodeAttribute nodeAttribute;

    @BeforeEach
    void setUp() {
        configuration = mock(SqlXConfiguration.class);
        datasourceManager = new DatasourceManager(configuration);
        dataSourceWrapper = mock(DataSourceWrapper.class);
        nodeAttribute = mock(NodeAttribute.class);

        when(dataSourceWrapper.getNodeAttribute()).thenReturn(nodeAttribute);
    }

    @Test
    void testAddAndGetDataSource() {
        String dataSourceName = "TestDataSource";
        datasourceManager.addDataSource(dataSourceName, dataSourceWrapper);

        DataSourceWrapper retrievedDataSource = datasourceManager.getDataSource(dataSourceName);
        assertNotNull(retrievedDataSource);
        assertEquals(dataSourceWrapper, retrievedDataSource);
    }

    @Test
    void testRemoveDataSource() {
        String dataSourceName = "TestDataSource";
        datasourceManager.addDataSource(dataSourceName, dataSourceWrapper);
        when(configuration.removeDataSourceConfiguration(dataSourceName)).thenReturn(true);

        datasourceManager.removeDataSource(dataSourceName);
        assertThrows(NoSuchDataSourceException.class, () -> datasourceManager.getDataSource(dataSourceName));
    }

    @Test
    void testRemoveNonExistentDataSource() {
        String dataSourceName = "NonExistentDataSource";
        assertThrows(ManagementException.class, () -> datasourceManager.removeDataSource(dataSourceName));
    }

    @Test
    void testGetDefaultDataSource() {
        when(dataSourceWrapper.getDefaulted()).thenReturn(true);
        datasourceManager.addDataSource("DefaultDataSource", dataSourceWrapper);

        DataSourceWrapper defaultDataSource = datasourceManager.getDefaultDataSource();
        assertNotNull(defaultDataSource);
        assertEquals(dataSourceWrapper, defaultDataSource);
    }

    @Test
    void testGetDefaultDataSourceWhenNoneExists() {
        assertThrows(SqlXRuntimeException.class, () -> datasourceManager.getDefaultDataSource());
    }

    @Test
    void testGetDataSourceList() {
        datasourceManager.addDataSource("DataSource1", dataSourceWrapper);
        List<DataSourceWrapper> dataSourceList = datasourceManager.getDataSourceList();

        assertNotNull(dataSourceList);
        assertEquals(1, dataSourceList.size());
        assertEquals(dataSourceWrapper, dataSourceList.get(0));
    }

    @Test
    void testSetNodeState() {
        String dataSourceName = "TestDataSource";
        datasourceManager.addDataSource(dataSourceName, dataSourceWrapper);

        datasourceManager.setNodeState(dataSourceName, NodeState.DOWN);
        verify(nodeAttribute).setNodeState(NodeState.DOWN);
    }

    @Test
    void testSetNodeWeight() {
        String dataSourceName = "TestDataSource";
        datasourceManager.addDataSource(dataSourceName, dataSourceWrapper);

        datasourceManager.setNodeWeight(dataSourceName, 0.5);
        verify(nodeAttribute).setNodeWeight(0.5);
    }

    @Test
    void testIsSameDatabaseProduct() {
        datasourceManager.addDataSource("DataSource1", dataSourceWrapper);
        when(nodeAttribute.getNodeState()).thenReturn(NodeState.UP);
        when(nodeAttribute.getDatabaseType()).thenReturn("MySQL");
        assertTrue(datasourceManager.isSameDatabaseProduct());

        DataSourceWrapper anotherDataSourceWrapper = mock(DataSourceWrapper.class);
        NodeAttribute anotherNodeAttribute = mock(NodeAttribute.class);
        when(anotherNodeAttribute.getDatabaseType()).thenReturn("PostgreSQL");
        when(anotherDataSourceWrapper.getNodeAttribute()).thenReturn(anotherNodeAttribute);
        datasourceManager.addDataSource("DataSource2", anotherDataSourceWrapper);

        assertFalse(datasourceManager.isSameDatabaseProduct());
    }
}