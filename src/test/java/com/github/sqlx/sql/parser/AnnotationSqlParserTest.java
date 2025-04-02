package com.github.sqlx.sql.parser;

import com.github.sqlx.NodeType;
import com.github.sqlx.config.SqlXConfiguration;
import com.github.sqlx.sql.AnnotationSqlAttribute;
import com.github.sqlx.sql.SqlAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @see AnnotationSqlParser
 * @since 1.0
 */
class AnnotationSqlParserTest {

    static SqlParser sqlParser = new AnnotationSqlParser(new JSqlParser(new SqlXConfiguration()), new DefaultAnnotationSqlHintParser());

    static SqlHintConverter<NodeType> sqlHintConverter = new NodeTypeSqlHintConverter();


    @Test
    void parse_1() {

        String sql = "/*!nodeType=write;xxx=ok;timeout=30s;*/ select * from employee where id = ?";
        SqlAttribute attribute = sqlParser.parse(sql);
        assertThat(attribute).isNotNull().isInstanceOf(AnnotationSqlAttribute.class);
        AnnotationSqlAttribute annotationSqlAttribute = (AnnotationSqlAttribute) attribute;
        assertThat(annotationSqlAttribute.getSqlHint()).isNotNull()
                .extracting(hint -> sqlHintConverter.convert(hint)).extracting(NodeType::canWrite).isEqualTo(true);

    }

    @Test
    void parse_2() {

        String sql = "/*!nodeType=write;*/ select * from employee where id = ?";
        SqlAttribute attribute = sqlParser.parse(sql);
        assertThat(attribute).isNotNull().isInstanceOf(AnnotationSqlAttribute.class);
        AnnotationSqlAttribute annotationSqlAttribute = (AnnotationSqlAttribute) attribute;
        assertThat(annotationSqlAttribute.getSqlHint()).isNotNull()
                .extracting(hint -> sqlHintConverter.convert(hint)).extracting(NodeType::canWrite).isEqualTo(true);

    }

    @Test
    void parse_3() {

        String sql = "/*!nodeType=write */ select * from employee where id = ?";
        SqlAttribute attribute = sqlParser.parse(sql);
        assertThat(attribute).isNotNull().isInstanceOf(AnnotationSqlAttribute.class);
        AnnotationSqlAttribute annotationSqlAttribute = (AnnotationSqlAttribute) attribute;
        assertThat(annotationSqlAttribute.getSqlHint()).isNotNull()
                .extracting(hint -> sqlHintConverter.convert(hint)).extracting(NodeType::canWrite).isEqualTo(true);

    }
}