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
            SqlExecutor.execute(null, "node1", "node2");
        });
    }

    @Test
    public void execute_BlankClusterAndEmptyNodes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SqlExecutor.execute(() -> "result",  new String[]{});
        });
    }

    @Test
    public void execute_ValidNodesOnly_ReturnsExpectedResult() {
        String result = SqlExecutor.execute(() -> "result",  "node1", "node2");
        assertEquals("result", result);
    }
}