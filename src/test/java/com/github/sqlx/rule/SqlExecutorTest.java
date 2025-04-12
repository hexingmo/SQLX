package com.github.sqlx.rule;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlExecutorTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void execute_NullFunction_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SqlExecutor.execute(null, "cluster", "node1", "node2");
        });
    }

    @Test
    public void execute_BlankClusterAndEmptyNodes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SqlExecutor.execute(() -> "result", "", new String[]{});
        });
    }

    @Test
    public void execute_ValidClusterAndNodes_ReturnsExpectedResult() {
        String result = SqlExecutor.execute(() -> "result", "cluster", "node1", "node2");
        assertEquals("result", result);
    }

    @Test
    public void execute_ValidClusterOnly_ReturnsExpectedResult() {
        String result = SqlExecutor.execute(() -> "result", "cluster");
        assertEquals("result", result);
    }

    @Test
    public void execute_ValidNodesOnly_ReturnsExpectedResult() {
        String result = SqlExecutor.execute(() -> "result", "", "node1", "node2");
        assertEquals("result", result);
    }
}