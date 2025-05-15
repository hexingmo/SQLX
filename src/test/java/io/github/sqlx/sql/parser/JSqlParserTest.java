package io.github.sqlx.sql.parser;

import io.github.sqlx.sql.SqlAttribute;
import io.github.sqlx.sql.SqlType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author He Xing Mo
 * @since 1.0
 *
 * @see JSqlParser
 */

@State(Scope.Benchmark)
@Slf4j
public class JSqlParserTest {

    static SqlParser sqlParser = new JSqlParser();

    @BeforeAll
    static void init() {

    }

    @Test
    public void testBenchmark() throws Exception {
        Options opt = new OptionsBuilder()
                .include(JSqlParserTest.class.getSimpleName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(0)
                //.threads(Runtime.getRuntime().availableProcessors() * 16)
                .threads(1)
                .syncIterations(true)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .verbosity(VerboseMode.EXTRA)
                .resultFormat(ResultFormatType.JSON)
                .output("./JSqlParser_Benchmark.json")
                .build();

        new Runner(opt).run();

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseJoin() {

        log.info("testing parse join select sql");

        String sql = "SELECT customers.customer_name, orders.order_id, order_details.product_name, order_details.unit_price, " +
                "order_details.quantity, (order_details.unit_price * order_details.quantity) AS total_price\n" +
                "FROM customers\n" +
                "INNER JOIN orders\n" +
                "ON customers.customer_id = orders.customer_id\n" +
                "INNER JOIN order_details\n" +
                "ON orders.order_id = order_details.order_id\n" +
                "WHERE customers.country = 'USA'\n" +
                "AND orders.order_date BETWEEN '2022-01-01' AND '2022-12-31'\n" +
                "AND order_details.quantity > 10\n" +
                "ORDER BY customers.customer_name ASC, orders.order_date DESC;\n";

        SqlAttribute attribute = sqlParser.parse(sql);
        assertThat(attribute).isNotNull();
        assertThat(attribute.isWrite()).isFalse();
        assertThat(attribute.isRead()).isTrue();
        assertThat(attribute.getSimpleFromTables()).contains("customers");
        assertThat(attribute.getSimpleJoinTables()).containsAll(Arrays.asList("orders", "order_details"));
        assertThat(attribute.getSimpleSubTables()).isEmpty();
        assertThat(attribute.getDatabases()).isEmpty();
        assertThat(attribute.getSqlType()).isEqualTo(SqlType.SELECT);

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseSubSelect() {

        log.info("testing parse sub select sql");

        String sql = "SELECT s1.student_name, s1.student_id, s1.grade\n" +
                "FROM students s1\n" +
                "INNER JOIN (\n" +
                "    SELECT AVG(grade) AS avg_grade\n" +
                "    FROM students\n" +
                "    WHERE grade >= 60\n" +
                ") s2\n" +
                "ON s1.grade >= s2.avg_grade\n" +
                "WHERE s1.gender = 'F'\n" +
                "ORDER BY s1.grade DESC;\n";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isFalse();
        assertThat(statement.isRead()).isTrue();
        assertThat(statement.getSimpleFromTables()).containsOnly("students");
        assertThat(statement.getSimpleJoinTables()).isEmpty();
        assertThat(statement.getSimpleSubTables()).containsOnly("students");
        assertThat(statement.getDatabases()).isEmpty();
        assertThat(statement.getSqlType()).isEqualTo(SqlType.SELECT);

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseUnion() {

        log.info("testing parse union select sql");

        String sql = "select\n" +
                "        *,\n" +
                "        (select GROUP_CONCAT(sku) from py.p_tradedt where tradenid = t.nid) skuList\n" +
                "        from py.p_tradewait t\n" +
                "        where t.ordertime >=  DATE_FORMAT('2023-05-26 00:00:00.241','%Y-%m-%d %H:%i:%s') and\n" +
                "            DATE_FORMAT('2023-05-26 23:59:59.241','%Y-%m-%d %H:%i:%s') >= t.ordertime\n" +
                "        union all\n" +
                "        select\n" +
                "        *,\n" +
                "        (select GROUP_CONCAT(sku) from py.p_tradedt where tradenid = t.nid) skuList\n" +
                "        from py.p_tradesend t\n" +
                "        where t.ordertime >=  DATE_FORMAT('2023-05-26 00:00:00.241','%Y-%m-%d %H:%i:%s') and\n" +
                "            DATE_FORMAT('2023-05-26 23:59:59.241','%Y-%m-%d %H:%i:%s') >= t.ordertime\n" +
                "        union all\n" +
                "        select\n" +
                "        *,\n" +
                "        (select GROUP_CONCAT(sku) from py.p_tradedt where tradenid = t.nid) skuList\n" +
                "        from py.p_tradestock t\n" +
                "        where t.ordertime >=  DATE_FORMAT('2023-05-26 00:00:00.241','%Y-%m-%d %H:%i:%s') and\n" +
                "            DATE_FORMAT('2023-05-26 23:59:59.241','%Y-%m-%d %H:%i:%s') >= t.ordertime\n" +
                "        union all\n" +
                "        select\n" +
                "        *,\n" +
                "        (select GROUP_CONCAT(sku) from py.p_tradedt where tradenid = t.nid) skuList\n" +
                "        from py.p_trade t\n" +
                "        where t.ordertime >=  DATE_FORMAT('2023-05-26 00:00:00.241','%Y-%m-%d %H:%i:%s') and\n" +
                "            DATE_FORMAT('2023-05-26 23:59:59.241','%Y-%m-%d %H:%i:%s') >= t.ordertime";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isFalse();
        assertThat(statement.isRead()).isTrue();
        assertThat(statement.getSimpleFromTables()).containsOnly("p_tradewait", "p_tradesend", "p_tradestock", "p_trade");
        assertThat(statement.getSimpleTables()).containsOnly("p_tradedt", "p_tradewait", "p_tradesend", "p_tradestock", "p_trade");
        assertThat(statement.getSimpleJoinTables()).isEmpty();
        assertThat(statement.getSimpleSubTables()).containsOnly("p_tradedt");
        assertThat(statement.getDatabases()).containsOnly("py");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.SELECT);
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseJoinSubSelect() {

        log.info("testing parse join sub select sql");

        String sql = "SELECT\n" +
                "        'North America' as 'auth_region',\n" +
                "        t1.request_report_type,\n" +
                "        t1.num  'allnum',\n" +
                "        t2.num  'errorNum',\n" +
                "        t2.num / t1.num 'rate'\n" +
                "        FROM\n" +
                "        ( SELECT request_report_type, count(1) num FROM t_amazon_report_log WHERE\n" +
                "        created_time>DATE_SUB(now(),INTERVAL 3 hour) and created_time <= DATE_SUB(now(),INTERVAL 1 hour) and auth_region=1\n" +
                "        GROUP BY request_report_type ) t1\n" +
                "        LEFT JOIN (\n" +
                "        SELECT\n" +
                "        request_report_type,\n" +
                "        count( 1 ) num\n" +
                "        FROM\n" +
                "        t_amazon_report_log\n" +
                "        WHERE\n" +
                "        IFNULL( report_id, '' ) = ''\n" +
                "        AND created_time>DATE_SUB(now(),INTERVAL 3 hour) and created_time <= DATE_SUB(now(),INTERVAL 1 hour) and auth_region=1 and processing_status in(0,2,4,5,6,7)\n" +
                "        GROUP BY\n" +
                "        request_report_type\n" +
                "        ) t2 ON t1.request_report_type = t2.request_report_type\n" +
                "\n" +
                "        UNION all\n" +
                "\n" +
                "        SELECT\n" +
                "        'Europe'  as 'auth_region',\n" +
                "        t1.request_report_type,\n" +
                "        t1.num  'allnum',\n" +
                "        t2.num  'errorNum',\n" +
                "        t2.num / t1.num 'rate'\n" +
                "        FROM\n" +
                "        ( SELECT request_report_type, count(1) num FROM t_amazon_report_log WHERE\n" +
                "        created_time>DATE_SUB(now(),INTERVAL 3 hour) and created_time <= DATE_SUB(now(),INTERVAL 1 hour) and auth_region=2\n" +
                "        GROUP BY request_report_type ) t1\n" +
                "        LEFT JOIN (\n" +
                "        SELECT\n" +
                "        request_report_type,\n" +
                "        count( 1 ) num\n" +
                "        FROM\n" +
                "        t_amazon_report_log\n" +
                "        WHERE\n" +
                "        IFNULL( report_id, '' ) = ''\n" +
                "        AND created_time>DATE_SUB(now(),INTERVAL 3 hour) and created_time <= DATE_SUB(now(),INTERVAL 1 hour) and auth_region=2 and processing_status in(0,2,4,5,6,7)\n" +
                "        GROUP BY\n" +
                "        request_report_type\n" +
                "        ) t2 ON t1.request_report_type = t2.request_report_type";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isFalse();
        assertThat(statement.isRead()).isTrue();
        assertThat(statement.getSimpleFromTables()).containsOnly("t_amazon_report_log");
        assertThat(statement.getSimpleTables()).containsOnly("t_amazon_report_log");
        assertThat(statement.getSimpleJoinTables()).isEmpty();
        assertThat(statement.getSimpleSubTables()).containsOnly("t_amazon_report_log");
        assertThat(statement.getDatabases()).isEmpty();
        assertThat(statement.getSqlType()).isEqualTo(SqlType.SELECT);

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseMultiJoin() {

        log.info("testing parse multi join select sql");

        String sql = "SELECT\n" +
                "        ub.team_name as teamName,\n" +
                "        ub.team_id as teamId,\n" +
                "        ub.company_name as companyName,\n" +
                "        ub.company_id as companyId,\n" +
                "        ub.name as userName,\n" +
                "        b.created_by as userId,\n" +
                "        b.purchase_number as purchaseNumber,\n" +
                "        b.purchase_status as purchaseStatus,\n" +
                "        DATE_FORMAT(b.created_time,'%Y-%m-%d') as createDay,\n" +
                "        pg.CostPrice as costPrice,\n" +
                "        s.tax_price as taxPrice\n" +
                "        FROM\n" +
                "        t_buy_purchase_demand b\n" +
                "        left join t_py_goods pg on b.sku=pg.sku\n" +
                "        left join  t_user_baseinfo ub on b.created_by = ub.id\n" +
                "        LEFT JOIN   t_buy_stock_order a  ON a.id = b.purchase_order_id\n" +
                "        left join t_buy_stock_order_details s on a.id=s.order_id and b.sku=s.sku\n" +
                "        WHERE\n" +
                "        1=1\n" +
                "        and b.has_file = 0\n" +
                "        and b.created_time >= concat( '2023-05-12',' 00:00:00')\n" +
                "        and b.created_time <= concat( '2023-05-27',' 23:59:59')\n" +
                "        and ub.department_name = 'Amazon'\n" +
                "\t    and b.order_platform not like concat('%','sampling','%')\n" +
                "        and b.purchase_status in\n" +
                "         (2 , 4 , 5 , 676 , 3423 , 3546 , 600 , 2545)";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isFalse();
        assertThat(statement.isRead()).isTrue();
        assertThat(statement.getSimpleFromTables()).containsOnly("t_buy_purchase_demand");
        assertThat(statement.getSimpleTables()).containsOnly("t_buy_purchase_demand", "t_py_goods", "t_user_baseinfo", "t_buy_stock_order", "t_buy_stock_order_details");
        assertThat(statement.getSimpleJoinTables()).containsOnly("t_py_goods", "t_user_baseinfo", "t_buy_stock_order", "t_buy_stock_order_details");
        assertThat(statement.getSimpleSubTables()).isEmpty();
        assertThat(statement.getDatabases()).isEmpty();
        assertThat(statement.getSqlType()).isEqualTo(SqlType.SELECT);
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseInsertIncludeSubSelect() {

        log.info("testing parse insert include sub select sql");

        String sql = "INSERT INTO table1 (column1, column2, column3)\n" +
                "VALUES ('value1', 'value2', (SELECT id FROM table2 WHERE name='value3'));";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("table1", "table2");
        assertThat(statement.getSimpleFromTables()).containsOnly("table1");
        assertThat(statement.getSimpleJoinTables()).isEmpty();
        assertThat(statement.getSimpleSubTables()).containsOnly("table2");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.INSERT);

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseInsertJoinSelect() {

        log.info("testing parse insert join sql");

        String sql = "INSERT INTO employees (first_name, last_name, job_id, hire_date, salary, commission_pct, manager_id, department_id)\n" +
                "SELECT e.first_name, e.last_name, e.job_id, e.hire_date, e.salary, e.commission_pct, e.manager_id, e.department_id\n" +
                "FROM employees e\n" +
                "JOIN departments d ON e.department_id = d.department_id\n" +
                "WHERE d.location_id = '1700';";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("employees", "departments");
        assertThat(statement.getSimpleFromTables()).containsOnly("employees");
        assertThat(statement.getSimpleJoinTables()).containsOnly("departments");
        assertThat(statement.getSimpleInsertTables()).containsOnly("employees");
        assertThat(statement.getSimpleSubTables()).isEmpty();
        assertThat(statement.getSqlType()).isEqualTo(SqlType.INSERT);

    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseInsertJoinIncludeSubSelect() {

        log.info("testing parse insert join include sub select sql");

        String sql = "INSERT INTO purchase_order (product_id, supplier_id, price, quantity)\n" +
                "SELECT p.id, s.id, (SELECT price FROM supplier_price WHERE supplier_id = s.id AND product_id = p.id AND status='ACTIVE'), 200\n" +
                "FROM products p, suppliers s\n" +
                "WHERE p.name = 'Product1' AND s.name = 'Supplier1';";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("purchase_order", "supplier_price", "products", "suppliers");
        assertThat(statement.getSimpleFromTables()).containsOnly("products" );
        assertThat(statement.getSimpleJoinTables()).containsOnly("suppliers");
        assertThat(statement.getSimpleSubTables()).containsOnly("supplier_price");
        assertThat(statement.getSimpleInsertTables()).containsOnly("purchase_order");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.INSERT);
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseUpdateIncludeSubSelect() {

        log.info("testing parse update include sub select sql");

        String sql = "UPDATE table1 \n" +
                "SET column1 = value1, column2 = value2, column3 = (SELECT column4 FROM table2 WHERE column5 = value3)\n" +
                "WHERE column6 IN (SELECT column7 FROM table3 WHERE column8 = value4);";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("table1", "table2", "table3");
        assertThat(statement.getSimpleFromTables()).containsOnly("table1");
        assertThat(statement.getSimpleJoinTables()).isEmpty();
        assertThat(statement.getSimpleSubTables()).containsOnly("table2", "table3");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.UPDATE);
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseUpdateJoinIncludeSubSelect() {

        log.info("testing parse update join include sub select sql");

        String sql = "UPDATE employees e\n" +
                "SET e.salary = e.salary * 1.1\n" +
                "WHERE e.department_id IN (\n" +
                "    SELECT d.department_id \n" +
                "    FROM departments d\n" +
                "    JOIN locations l ON d.location_id = l.location_id \n" +
                "    WHERE l.city = 'New York'\n" +
                ");";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("employees", "departments", "locations");
        assertThat(statement.getSimpleFromTables()).containsOnly("employees");
        assertThat(statement.getSimpleJoinTables()).containsOnly("locations");
        assertThat(statement.getSimpleSubTables()).containsOnly("departments");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.UPDATE);
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 200, time = 5, timeUnit = TimeUnit.MILLISECONDS)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Test
    public void testParseDeleteIncludeJoinAndSubSelect() {
        log.info("testing parse delete include join and sub select sql");
        String sql = "DELETE FROM orders\n" +
                "WHERE product_id IN (\n" +
                "    SELECT product_id\n" +
                "    FROM (\n" +
                "        SELECT product_id, SUM(quantity_ordered) AS total_quantity\n" +
                "        FROM order_details\n" +
                "        GROUP BY product_id\n" +
                "        HAVING total_quantity < (\n" +
                "            SELECT AVG(quantity_ordered)\n" +
                "            FROM order_details\n" +
                "        )\n" +
                "    ) AS subquery\n" +
                "    JOIN products ON products.id = subquery.product_id\n" +
                "    WHERE products.category_id = 2\n" +
                ");";

        SqlAttribute statement = sqlParser.parse(sql);
        assertThat(statement).isNotNull();
        assertThat(statement.isWrite()).isTrue();
        assertThat(statement.isRead()).isFalse();
        assertThat(statement.getSimpleTables()).containsOnly("orders", "order_details", "products");
        assertThat(statement.getSimpleFromTables()).containsOnly("orders");
        assertThat(statement.getSimpleJoinTables()).containsOnly("products");
        assertThat(statement.getSimpleSubTables()).containsOnly("order_details");
        assertThat(statement.getSqlType()).isEqualTo(SqlType.DELETE);
    }

    @Test
    void test1() {
        String sql = "select\n" +
                "\n" +
                "    id, platform_code, station_code, shop_id, shop_name, order_no, original_order_id, order_state, sale_type, sale_channel, platform_order_id, transaction_id, transaction_type, buyer_id, buyer_account, seller_id, guid, pending_reason,reason_code,(select group_concat(tag_name) from t_tag_type where FIND_IN_SET(id,info.reason_code)) as reasonCodeName, memo, order_time, download_time, last_send_time, closing_date, closing_user, times_tamp, delay_days, cancellation_time, cancellation_memo, sku_num, multi_item, total_weight, good_item_ids, default_sku_location, all_goods_detail, protection_eligibility, operation_notes, suggest_merge_tag, is_merge_bill, is_split_bill, is_deliver, express_status, express_fare_close, shipping_status, evaluate_status, trans_mail, print_flag, payer_email, payer_country_code, payer_country_name, payer_status, payer_business, payer_first_name, payer_middle_name, payer_last_name, payment_type, pay_ment_status, subject, payer_id, note, ship_to_name, ship_to_country_code, ship_to_country_name, ship_to_state, ship_to_city, ship_to_street, ship_to_street2, doorplate, ship_to_zip, ship_to_phone_num, id_number, tax_number, ioss_number, receiver_business, receiver_email, order_amt, order_amt_cny, amt, amt_cny, ship_discount, ship_discount_cny, fee_amt, fee_amt_cny, advertising_expenses, advertising_expenses_cny, shipping_amt, shipping_amt_cny, order_discount, order_discount_cny, tax_amt, tax_amt_cny, ship_amount, ship_amount_cny, ship_handle_amount, ship_handle_amount_cny, express_fare, logics_express_fare, update_logics_express_fare_time, goods_costs, inner_packag_amount, outer_packag_amount, currency_code, ex_change_rate, exchange_loss_amount_cny, exchange_loss_proportion, gross_profit, gross_profit_margin, shipped_gross_profit, shipped_gross_profit_margin, profit_money, handling_amt, logics_way_id, logics_way_name, logics_way_group_code, logics_way_group_name, logics_way_get_type, logics_rule_id, logics_rule_name, express_nid, express_name, shop_logics_way_name, logistics_type, logistics_service_name, face_sheet_size, track_no, track_no_status, track_no_create_time, track_no_expire_time, out_order_code, abnormal_cause, abnormal_sku_not_associated, abnormal_sku_not_associated_reason, abnormal_negative_profit, abnormal_negative_profit_reason, abnormal_logistics_ex, abnormal_logistics_ex_reason, abnormal_address_ex, abnormal_address_ex_reason, abnormal_big_order_ex, abnormal_big_order_ex_reason, abnormal_overweight_ex, abnormal_overweight_ex_reason, abnormal_leaving_message, abnormal_leaving_message_reason, abnormal_data_missing_ex, abnormal_data_missing_ex_reason, agree_negative_profit, agree_leaving_message, agree_address_ex, agree_big_order_ex, agree_overweight_ex, abnormal_agree_user_id,abnormal_agree_time,abnormal_agree_user_name,store_id, store_name, restore_stock, reservation_rule_id, reservation_rule_priority, reservation_rule_time, color_flag, is_checked, check_order, paidan_men, paidan_date, batch_num, picking_time, picking_remarks, packing_men, shoot_men, shoot_time, box_num, package_men, is_package, scanning_men, scanning_date, weighing_men, weighing_date, actual_weight, logistics_transfer_men, logistics_transfer_time, delivery_no, delivery_user, delivery_time, handover_status, delivered_status, logistics_days, logistics_weighing, business, sel_flag, additional_charge, clean_flag, transfer_to_warehouse_men, transfer_to_warehouse_date, transfer_to_delivering_men, transfer_to_delivering_date, enabled, created_by, created_by_name, created_time, last_update_by, last_update_time, last_update_user_name\n" +
                "     ,(select name from `longpean-prod`.t_user_baseinfo where id = info.delivery_user) as deliveryUserStr\n" +
                "from\n" +
                "    t_oms_shipped_order_info info\n" +
                "\n" +
                "WHERE  info.clean_flag = 1\n" +
                "\n" +
                "  AND info.order_time >= '2023-08-03 00:00:00'\n" +
                "\n" +
                "\n" +
                "\n" +
                "  AND info.order_time <= '2023-08-09 23:59:59';";

        String chopSql = StringUtils.replaceEach(sql , new String[]{StringUtils.LF , StringUtils.CR} , new String[]{" " , " "});
        SqlAttribute statement = sqlParser.parse(chopSql);
        System.out.println(statement);

    }
}