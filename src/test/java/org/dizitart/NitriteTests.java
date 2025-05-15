package org.dizitart;

import io.github.sqlx.metrics.SqlMetrics;
//import com.github.devx.sql.routing.metrics.nitrite.SqlMetricsDecorator;
import io.github.sqlx.util.JsonUtils;
import io.github.sqlx.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
//import org.dizitart.no2.collection.Document;
//import org.dizitart.no2.common.WriteResult;
//import org.dizitart.no2.mvstore.MVStoreModule;
//import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
//import org.dizitart.no2.repository.Cursor;
//import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author He Xing Mo
 * @since 1.0
 */
@Slf4j
class NitriteTests {

    @Test
    void test1() throws Exception {
        Nitrite db = Nitrite.builder()
                .filePath("D:\\test\\SqlMetrics.db")
                .compressed()
                .openOrCreate();
        ObjectRepository<SqlMetrics> repository = db.getRepository(SqlMetrics.class);
        //ObjectFilter filter = ObjectFilters.and(
        //        ObjectFilters.gte("createdTime", 1),
        //        ObjectFilters.lte("createdTime", 1)
        //);
        //FindOptions findOptions = FindOptions.sort("createdTime", SortOrder.Descending, NullOrder.Last)
        //        .thenLimit(0 , 10);
        //Cursor<SqlMetrics> cursor = repository.find(findOptions);
        //List<SqlMetrics> list = cursor.toList();
        //log.info("list -> {}" , list);
        String raw = "{\"statementId\":\"ef8bb86f447248b7b298f95e94ee14aa\",\"sql\":\"/*!nodeName\\u003dread_1;*/ select * from area where id \\u003d ?\",\"nativeSql\":\"select * from area where id \\u003d ?\",\"sqlType\":\"SELECT\",\"isWrite\":false,\"isRead\":true,\"clusterName\":\"cluster_0\",\"node\":{\"url\":\"jdbc:h2:mem:~/test3;FILE_LOCK\\u003dSOCKET;DB_CLOSE_DELAY\\u003d-1;DB_CLOSE_ON_EXIT\\u003dTRUE;AUTO_RECONNECT\\u003dTRUE;IGNORECASE\\u003dTRUE;\",\"nodeType\":\"READ\",\"nodeState\":\"UP\",\"name\":\"read_1\",\"weight\":10.0},\"databases\":[\"TEST3\"],\"tables\":[\"area\"],\"executeTimeElapsedMillis\":0.07,\"succeeded\":true,\"exception\":null,\"updateRows\":0,\"selectedRows\":1,\"transactionId\":null,\"transactionName\":null,\"createdTime\":1741944172361,\"updatedTime\":1741944172361}";
        SqlMetrics sqlMetrics = JsonUtils.fromJson(raw, SqlMetrics.class);
        for (int i = 0; i < 10000; i++) {
            sqlMetrics.setStatementId(UUIDUtils.getSimpleUUID());
            sqlMetrics.setCreatedTime(System.currentTimeMillis());
            sqlMetrics.setUpdatedTime(System.currentTimeMillis());
            log.info("insert sqlMetrics {}", i);
            repository.insert(sqlMetrics);
        }
    }

    @Test
    void test() throws Exception {

        Nitrite db = Nitrite.builder()
                .filePath("D:\\OpenProject\\sql-routing\\src\\test\\sql_routing.db")
                .compressed()
                .openOrCreate("user", "password");

        ObjectRepository<SqlMetrics> repository = db.getRepository(SqlMetrics.class);

        SqlMetrics sqlMetrics = new SqlMetrics();
        Set<String> tables = new HashSet<>();
        tables.add("employee");
        sqlMetrics.setSql("select * from employee where id = ?")
                .setDatabases(null)
                .setTables(tables)
                .setCreatedTime(System.currentTimeMillis());
        repository.insert(sqlMetrics);

        Cursor<SqlMetrics> cursor = repository.find();
        Iterator<SqlMetrics> iterator = cursor.iterator();
        while (iterator.hasNext()) {
            log.info("each -> {}" , iterator.next());
        }


        ObjectFilter filter = ObjectFilters.and(
                //ObjectFilters.eq("database", "ax"),
                ObjectFilters.elemMatch("tables", ObjectFilters.eq("tables" , "employee"))
        );
        Cursor<SqlMetrics> cursor1 = repository.find(filter);
        Iterator<SqlMetrics> iterator1 = cursor1.iterator();
        while (iterator1.hasNext()) {
            log.info("each matched -> {}" , iterator1.next());
        }
    }
}
