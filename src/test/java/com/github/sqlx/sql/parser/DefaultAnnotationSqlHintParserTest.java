package com.github.sqlx.sql.parser;

import com.github.sqlx.exception.SqlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultAnnotationSqlHintParser}.
 * This class tests the behavior of DefaultAnnotationSqlHintParser when parsing SQL with annotations.
 */
class DefaultAnnotationSqlHintParserTest {

    private DefaultAnnotationSqlHintParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultAnnotationSqlHintParser();
    }

    @Test
    void testParseWithValidHint() {
        String sql = "/*!nodeName=write_0;*/ select * from employee where id = 1";
        SqlHint sqlHint = parser.parse(sql);

        assertThat(sqlHint).isNotNull();
        assertThat(sqlHint.getNativeSql()).isEqualTo("select * from employee where id = 1");
        Map<String, String> hints = sqlHint.getHints();
        assertThat(hints).containsEntry("nodeName", "write_0");
    }

    @Test
    void testParseWithNoHint() {
        String sql = "select * from employee where id = 1";
        SqlHint sqlHint = parser.parse(sql);

        assertThat(sqlHint).isNotNull();
        assertThat(sqlHint.getNativeSql()).isEqualTo(sql);
        assertThat(sqlHint.getHints()).isEmpty();
    }

    @Test
    void testParseWithInvalidHintFormat() {
        String sql = "select * from employee /*! nodeName write_0 */ where id = 1";

        assertThatThrownBy(() -> parser.parse(sql))
            .isInstanceOf(SqlParseException.class)
            .hasMessageContaining("hint key and value in SQL annotations must be separated by an equal sign (=)");
    }

    @Test
    void testParseWithEmptySql() {
        String sql = "";
        SqlHint sqlHint = parser.parse(sql);

        assertThat(sqlHint).isNotNull();
        assertThat(sqlHint.getNativeSql()).isEqualTo(sql);
        assertThat(sqlHint.getHints()).isEmpty();
    }

    @Test
    void testParseWithNullSql() {
        SqlHint sqlHint = parser.parse(null);

        assertThat(sqlHint).isNotNull();
        assertThat(sqlHint.getNativeSql()).isNull();
        assertThat(sqlHint.getHints()).isEmpty();
    }
}