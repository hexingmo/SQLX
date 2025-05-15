package io.github.sqlx.sql.parser;

import io.github.sqlx.config.SqlParsingFailBehavior;
import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.DefaultSqlAttribute;
import io.github.sqlx.sql.SqlType;
import io.github.sqlx.exception.SqlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FailBehaviorSqlParser}.
 * This class tests the behavior of FailBehaviorSqlParser when parsing succeeds or fails.
 * Author: He Xing Mo
 * Version: 1.0
 */
class FailBehaviorSqlParserTest {

    private SqlParser mockDelegateParser;
    private FailBehaviorSqlParser failBehaviorSqlParser;

    @BeforeEach
    void setUp() {
        mockDelegateParser = mock(SqlParser.class);
    }

    @Test
    void testParseSuccess() {
        String sql = "SELECT * FROM students;";
        SqlAttribute expectedAttribute = new DefaultSqlAttribute().setSql(sql).setRead(true).setSqlType(SqlType.SELECT);

        when(mockDelegateParser.parse(sql)).thenReturn(expectedAttribute);

        failBehaviorSqlParser = new FailBehaviorSqlParser(mockDelegateParser, SqlParsingFailBehavior.IGNORE);
        SqlAttribute result = failBehaviorSqlParser.parse(sql);

        assertThat(result).isEqualTo(expectedAttribute);
        verify(mockDelegateParser, times(1)).parse(sql);
    }

    @Test
    void testParseFailureWithIgnoreBehavior() {
        String sql = "SELECT * FROM students;";
        Exception parseException = new RuntimeException("Parsing failed");

        when(mockDelegateParser.parse(sql)).thenThrow(parseException);

        failBehaviorSqlParser = new FailBehaviorSqlParser(mockDelegateParser, SqlParsingFailBehavior.IGNORE);
        SqlAttribute result = failBehaviorSqlParser.parse(sql);

        assertThat(result.isWrite()).isTrue();
        assertThat(result.getSqlType()).isEqualTo(SqlType.OTHER);
        verify(mockDelegateParser, times(1)).parse(sql);
    }

    @Test
    void testParseFailureWithWarningBehavior() {
        String sql = "SELECT * FROM students;";
        Exception parseException = new RuntimeException("Parsing failed");

        when(mockDelegateParser.parse(sql)).thenThrow(parseException);

        failBehaviorSqlParser = new FailBehaviorSqlParser(mockDelegateParser, SqlParsingFailBehavior.WARNING);
        SqlAttribute result = failBehaviorSqlParser.parse(sql);

        assertThat(result.isWrite()).isTrue();
        assertThat(result.getSqlType()).isEqualTo(SqlType.OTHER);
        verify(mockDelegateParser, times(1)).parse(sql);
    }

    @Test
    void testParseFailureWithFailingBehavior() {
        String sql = "SELECT * FROM students;";
        Exception parseException = new RuntimeException("Parsing failed");

        when(mockDelegateParser.parse(sql)).thenThrow(parseException);

        failBehaviorSqlParser = new FailBehaviorSqlParser(mockDelegateParser, SqlParsingFailBehavior.FAILING);

        assertThatThrownBy(() -> failBehaviorSqlParser.parse(sql))
            .isInstanceOf(SqlParseException.class)
            .hasMessageContaining("SQL Parse Error");
        
        verify(mockDelegateParser, times(1)).parse(sql);
    }
} 