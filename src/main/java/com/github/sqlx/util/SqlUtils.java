package com.github.sqlx.util;

import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SqlUtils {

    private SqlUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isAnnotationSql(SqlAttribute sqlAttribute) {
        if (AnnotationSqlAttribute.class.isAssignableFrom(sqlAttribute.getClass())) {
            return true;
        }
        return false;
    }
}
