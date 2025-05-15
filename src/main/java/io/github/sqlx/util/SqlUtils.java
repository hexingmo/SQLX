package io.github.sqlx.util;

import io.github.sqlx.sql.AnnotationSqlAttribute;
import io.github.sqlx.sql.SqlAttribute;

import java.util.Objects;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class SqlUtils {

    private SqlUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isAnnotationSql(SqlAttribute sqlAttribute) {
        boolean isAnnotationSql = false;
        if (AnnotationSqlAttribute.class.isAssignableFrom(sqlAttribute.getClass())) {
            AnnotationSqlAttribute asa = (AnnotationSqlAttribute) sqlAttribute;
            if (Objects.nonNull(asa.getSqlHint()) && MapUtils.isNotEmpty(asa.getSqlHint().getHints())) {
                isAnnotationSql = true;
            }
        }
        return isAnnotationSql;
    }
}
