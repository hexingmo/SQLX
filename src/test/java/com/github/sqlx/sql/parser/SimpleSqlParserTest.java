package com.github.sqlx.sql.parser;

import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.SqlAttribute;
import com.github.sqlx.sql.SqlType;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @since 1.0
 *
 * @see SimpleSqlParser
 */
class SimpleSqlParserTest {

    static SqlParser sqlParser = new SimpleSqlParser(new SqlXConfiguration());


    @Test
    void parseSelectTest() {
        String sql = " select * from students ";
        SqlAttribute sqlAttribute = sqlParser.parse(sql);
        assertThat(sqlAttribute).isNotNull();
        assertThat(sqlAttribute.isWrite()).isFalse();
        assertThat(sqlAttribute.isRead()).isTrue();
        assertThat(sqlAttribute.getSqlType()).isEqualTo(SqlType.SELECT);
    }

    @Test
    void parseInsertTest() {
        String sql = "INSERT INTO students (name, age) VALUES ('judi', 20);\n";
        SqlAttribute sqlAttribute = sqlParser.parse(sql);
        assertThat(sqlAttribute).isNotNull();
        assertThat(sqlAttribute.isWrite()).isTrue();
        assertThat(sqlAttribute.isRead()).isFalse();
        assertThat(sqlAttribute.getSqlType()).isEqualTo(SqlType.INSERT);
    }

    @Test
    void parseUpdateTest() {
        String sql = "UPDATE students  \n" +
                "SET age = 21  \n" +
                "WHERE name = 'judi';";
        SqlAttribute sqlAttribute = sqlParser.parse(sql);
        assertThat(sqlAttribute).isNotNull();
        assertThat(sqlAttribute.isWrite()).isTrue();
        assertThat(sqlAttribute.isRead()).isFalse();
        assertThat(sqlAttribute.getSqlType()).isEqualTo(SqlType.UPDATE);
    }

    @Test
    void parseDeleteTest() {
        String sql = "DELETE FROM students  \n" +
                "WHERE name = 'judi';";
        SqlAttribute sqlAttribute = sqlParser.parse(sql);
        assertThat(sqlAttribute).isNotNull();
        assertThat(sqlAttribute.isWrite()).isTrue();
        assertThat(sqlAttribute.isRead()).isFalse();
        assertThat(sqlAttribute.getSqlType()).isEqualTo(SqlType.DELETE);
    }
}