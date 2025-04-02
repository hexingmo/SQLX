package com.github.sqlx.sql.parser;

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSqlParser implements SqlParser {

    private SqlXConfiguration routingConf;

    protected AbstractSqlParser() {
    }

    protected AbstractSqlParser(SqlXConfiguration routingConf) {
        this.routingConf = routingConf;
    }

    @Override
    public SqlAttribute parse(String sql) {
        sql = StringUtils.replaceEach(sql , new String[]{StringUtils.LF , StringUtils.CR} , new String[]{" " , " "});

        SqlAttribute sqlAttr;
        try {
            sqlAttr = internalParse(sql.trim());
        } catch (Exception e) {
            sqlAttr = routingConf.getSqlParsingFailBehavior().action(sql, e);
        }
        return sqlAttr;
    }

    protected abstract SqlAttribute internalParse(String sql) throws Exception;
}
