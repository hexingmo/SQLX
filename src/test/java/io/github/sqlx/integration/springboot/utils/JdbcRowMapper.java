package io.github.sqlx.integration.springboot.utils;

import io.github.sqlx.integration.springboot.entity.Employee;
import io.github.sqlx.utils.TestUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * mapping rows of a ResultSet on a per-row basis.
 *
 * @author jing yun
 * @since 1.0
 */
public class JdbcRowMapper<T> implements RowMapper<T> {

    private Class<T> mapperClass;

    private Consumer<Mapper<T>> mapper;

    public JdbcRowMapper(Class<T> mapperClass, Consumer<Mapper<T>> mapper) {
        this.mapper = mapper;
        this.mapperClass = mapperClass;
    }

    public JdbcRowMapper() {
    }


    @Override
    public T mapRow(@NotNull ResultSet rs, int rowNum) throws SQLException {
        T returnValue = new BeanPropertyRowMapper<T>(mapperClass)
                .mapRow(rs, rowNum);
        if (Objects.isNull(mapper)) {
            return returnValue;
        }
        mapper.accept(new Mapper<T>()
                .setObject(returnValue)
                .setRs(rs)
                .setRowNum(rowNum));
        return returnValue;
    }


    @Data
    @Accessors(chain = true)
    public static class Mapper<T> {
        private T object;
        private ResultSet rs;
        private int rowNum;


        public <V> void mapVal(BiConsumer<T, V> setter, RSValueAccessor<V> getter, String columnName) {
            setter.accept(object, TestUtils.callInTry(() -> getter.getValue(columnName)));
        }

        public Long getLong(String columnName) {
            return TestUtils.callInTry(() -> rs.getLong(columnName));
        }
    }

    public interface RSValueAccessor<T> {
        T getValue(String columnName);
    }

    public static void main(String[] args) {
        new JdbcRowMapper<>(Employee.class, mapper ->
                mapper.mapVal(Employee::setDepartmentId, mapper::getLong, "department_id"));
    }


}
