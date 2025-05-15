package io.github.sqlx.sql.parser;

import io.github.sqlx.sql.DefaultSqlAttribute;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.SqlType;
import io.github.sqlx.util.StringUtils;

/**
 *
 * A simple SQL parser that only uses the "select, insert, update, delete" keyword at the
 * beginning of the SQL statement to determine whether the SQL statement is a read statement
 * or a write statement. It does not actually parse the SQL statement, so it is impossible
 * to obtain attributes such as the table name in the SQL statement.
 *
 * @author He Xing Mo
 * @since 1.0
 */
public class SimpleSqlParser implements SqlParser {

    @Override
    public SqlAttribute parse(String sql) {
        SqlType sqlType = SqlType.OTHER;
        boolean isRead = StringUtils.startsWithIgnoreCase(sql, "select");
        if (isRead) {
            sqlType = SqlType.SELECT;
        }
        boolean insert = StringUtils.startsWithIgnoreCase(sql, "insert");
        if (insert) {
            sqlType = SqlType.INSERT;
        }
        boolean update = StringUtils.startsWithIgnoreCase(sql, "update");
        if (update) {
            sqlType = SqlType.UPDATE;
        }
        boolean delete = StringUtils.startsWithIgnoreCase(sql, "delete");
        if (delete) {
            sqlType = SqlType.DELETE;
        }

        boolean isWrite = insert || update || delete;
        return new DefaultSqlAttribute().setSql(sql).setNativeSql(sql).setWrite(isWrite).setRead(isRead).setSqlType(sqlType);
    }
}
