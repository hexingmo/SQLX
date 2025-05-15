package io.github.sqlx.rule;

import io.github.sqlx.NodeAttribute;
import io.github.sqlx.jdbc.datasource.DataSourceWrapper;
import io.github.sqlx.jdbc.datasource.DatasourceManager;
import io.github.sqlx.sql.SqlAttribute;

import java.util.List;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SingleDatasourceRouteRule extends AbstractRouteRule {

    private final DatasourceManager datasourceManager;

    public SingleDatasourceRouteRule(Integer priority, DatasourceManager datasourceManager) {
        super(priority);
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
