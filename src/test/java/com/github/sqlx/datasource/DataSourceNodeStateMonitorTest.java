package com.github.sqlx.datasource;

import com.github.sqlx.NodeState;
import com.github.sqlx.NodeType;
import com.github.sqlx.jdbc.datasource.DataSourceAttribute;
import com.github.sqlx.jdbc.datasource.DataSourceNodeStateMonitor;
import com.github.sqlx.jdbc.datasource.DataSourceWrapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.LockSupport;

/**
 * @author He Xing Mo
 * @since 1.0
 */
class DataSourceNodeStateMonitorTest {

    final DataSourceNodeStateMonitor monitor = new DataSourceNodeStateMonitor();

    @Test
    void monitor() {

        JdbcDataSource ds0 = new JdbcDataSource();
        ds0.setUrl("jdbc:h2:mem:~/test0;");
        DataSourceWrapper dsw0 = new DataSourceWrapper("testDS-0" , ds0 , new DataSourceAttribute(ds0.getURL() , NodeType.READ, NodeState.UNKNOWN , "read-0" , 1.0 , "select 1" , 1000 , null) , true);
        monitor.monitor(dsw0);

        JdbcDataSource ds1 = new JdbcDataSource();
        ds1.setUrl("jdbc:h2:mem:~/test1;");
        DataSourceWrapper dsw1 = new DataSourceWrapper("testDS-1" , ds1 , new DataSourceAttribute(ds1.getURL() , NodeType.READ, NodeState.UNKNOWN , "read-1" , 1.0 , "select 2" , 2000 , null) , false);
        monitor.monitor(dsw1);

        LockSupport.park(Thread.currentThread());
    }
}