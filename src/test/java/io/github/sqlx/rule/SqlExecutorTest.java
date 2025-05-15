package io.github.sqlx.rule;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqlExecutorTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    void execute_NullFunction_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SqlExecutor.execute(null, "node1", "node2");
        });
    }

    @Test
    void execute_BlankClusterAndEmptyNodes_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SqlExecutor.execute(() -> "result",  new String[]{});
        });
    }

    @Test
    void execute_ValidNodesOnly_ReturnsExpectedResult() {
        String result = SqlExecutor.execute(() -> "result",  "node1", "node2");
        assertEquals("result", result);
    }
}