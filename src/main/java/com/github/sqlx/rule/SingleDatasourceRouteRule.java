package com.github.sqlx.rule;

import com.github.sqlx.NodeAttribute;
import com.github.sqlx.jdbc.datasource.DataSourceWrapper;
import com.github.sqlx.jdbc.datasource.DatasourceManager;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.parser.SqlParser;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SingleDatasourceRouteRule extends AbstractRouteRule {

    private final DatasourceManager datasourceManager;

    public SingleDatasourceRouteRule(Integer priority, SqlParser sqlParser, DatasourceManager datasourceManager) {
        super(priority, sqlParser);
        this.datasourceManager = datasourceManager;
    }

    @Override
    public NodeAttribute routing(SqlAttribute sqlAttribute) {
        List<DataSourceWrapper> dataSourceList = datasourceManager.getDataSourceList();
        if (dataSourceList != null && dataSourceList.size() == 1) {
            return dataSourceList.get(0).getNodeAttribute();
        }
        return null;
    }
}
