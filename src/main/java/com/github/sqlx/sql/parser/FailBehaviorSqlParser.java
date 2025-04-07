package com.github.sqlx.sql.parser;

import com.github.sqlx.config.SqlParsingFailBehavior;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.util.StringUtils;

public class FailBehaviorSqlParser implements SqlParser {

    private final SqlParser delegate;

    private final SqlParsingFailBehavior failBehavior;

    public FailBehaviorSqlParser(SqlParser delegate, SqlParsingFailBehavior failBehavior) {
        this.delegate = delegate;
        this.failBehavior = failBehavior;
    }

    @Override
    public SqlAttribute parse(String sql) {
        sql = StringUtils.replaceEach(sql , new String[]{StringUtils.LF , StringUtils.CR} , new String[]{" " , " "});

        SqlAttribute sqlAttr;
        try {
            sqlAttr = delegate.parse(sql.trim());
        } catch (Exception e) {
            sqlAttr = failBehavior.action(sql, e);
        }
        return sqlAttr;
    }
}
