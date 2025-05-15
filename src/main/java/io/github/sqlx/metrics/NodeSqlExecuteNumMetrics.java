package io.github.sqlx.metrics;


import lombok.Data;
import lombok.experimental.Accessors;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@Data
@Accessors(chain = true)
@Indices({
        @Index(value = "nodeName" ,type = IndexType.NonUnique),
        @Index(value = "timestamp" ,type = IndexType.NonUnique)
})
public class NodeSqlExecuteNumMetrics {

    @Id
    private String id;

    private Long timestamp;

    private String nodeName;

    private Integer selectCount;

    private Integer insertCount;

    private Integer updateCount;

    private Integer deleteCount;

    private Integer otherCount;

    private Long createdTime;
}
