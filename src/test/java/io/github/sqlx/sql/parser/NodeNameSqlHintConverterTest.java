package io.github.sqlx.sql.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NodeNameSqlHintConverter}.
 * This class tests the behavior of NodeNameSqlHintConverter when converting SQL hints.
 * @author He Xing Mo
 * @since 1.0
 */
class NodeNameSqlHintConverterTest {

    private NodeNameSqlHintConverter converter;

    @BeforeEach
    void setUp() {
        converter = new NodeNameSqlHintConverter();
    }

    @Test
    void testConvertWithValidNodeNameHint() {
        Map<String, String> hints = new HashMap<>();
        hints.put("nodeName", "node1");
        SqlHint sqlHint = new SqlHint();
        sqlHint.setHints(hints);

        String result = converter.convert(sqlHint);

        assertThat(result).isEqualTo("node1");
    }

    @Test
    void testConvertWithNoNodeNameHint() {
        Map<String, String> hints = new HashMap<>();
        SqlHint sqlHint = new SqlHint();
        sqlHint.setHints(hints);

        String result = converter.convert(sqlHint);

        assertThat(result).isNull();
    }

    @Test
    void testConvertWithNullHint() {
        String result = converter.convert(null);

        assertThat(result).isNull();
    }

    @Test
    void testConvertWithNullHintsMap() {
        SqlHint sqlHint = new SqlHint();
        String result = converter.convert(sqlHint);

        assertThat(result).isNull();
    }
}