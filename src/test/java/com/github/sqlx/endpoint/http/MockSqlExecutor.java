package com.github.sqlx.endpoint.http;

import com.github.sqlx.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MockSqlExecutor {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;


    public MockSqlExecutor(JdbcTemplate jdbcTemplate ,TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;

//        EXECUTOR.scheduleAtFixedRate(() -> {
//
//            transactionTemplate.executeWithoutResult(status -> {
//                Map<String, Object> result1 = jdbcTemplate.queryForObject("select * from employee where id = ?" , new Object[] {1} ,new ColumnMapRowMapper());
//                log.info("result1 {}" , result1);
//
//                Map<String, Object> result2 = jdbcTemplate.queryForObject("select * from area where id = ?" , new Object[] {1} ,new ColumnMapRowMapper());
//                log.info("result2 {}" , result2);
//
//                int row = jdbcTemplate.update("insert into employee (id , name , department_id) values (? , ? , ?)", ps -> {
//                    ps.setLong(1 , new Random().nextLong());
//                    ps.setString(2 , "He Xing Mo");
//                    ps.setInt(3 , 1);
//                });
//                log.info("inserted row {}" , row);
//            });
//
//
//        } , 1 ,13, TimeUnit.MILLISECONDS);


        EXECUTOR.scheduleAtFixedRate(() -> {

            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Map<String, Object> result1 = jdbcTemplate.queryForObject("select * from employee where id = ?" , new Object[] {1} ,new ColumnMapRowMapper());
                    log.info("result1 {}" , result1);

                    Map<String, Object> result2 = jdbcTemplate.queryForObject("select * from area where id = ?" , new Object[] {1} ,new ColumnMapRowMapper());
                    log.info("result2 {}" , result2);

                    int row = jdbcTemplate.update("insert into orders (id , order_no , pay_no) values (? , ? , ?)", ps -> {
                        ps.setLong(1 , new Random().nextLong());
                        ps.setString(2 , "order-1");
                        ps.setString(3 , "pay-1");
                    });
                    log.info("inserted row {}" , row);
                });
            } catch (Exception e) {

            }


        } , 1 ,13, TimeUnit.MILLISECONDS);

//        EXECUTOR.scheduleAtFixedRate(() -> {
//            String sql = "/*!nodeName=read_0;*/ select * from employee where id = ?";
//            Map<String, Object> result = jdbcTemplate.queryForObject(sql , new Object[] {1} ,new ColumnMapRowMapper());
//            log.info("result {}" , result);
//        } , 1 ,1, TimeUnit.MILLISECONDS);

        //EXECUTOR.scheduleAtFixedRate(() -> {
        //    try {
        //        String sql = "/*!nodeName=read_0;*/ select * from users where id = ?";
        //        Map<String, Object> result = jdbcTemplate.queryForObject(sql , new Object[] {1} ,new ColumnMapRowMapper());
        //        log.info("result {}" , result);
        //    } catch (Exception e) {
        //
        //    }
        //} , 1 ,19, TimeUnit.MILLISECONDS);

        //EXECUTOR.scheduleAtFixedRate(() -> {
        //    String sql = "/*!nodeName=read_1;*/ select * from area where id = ?";
        //    Map<String, Object> result = jdbcTemplate.queryForObject(sql , new Object[] {1} ,new ColumnMapRowMapper());
        //    log.info("result {}" , result);
        //} , 1 ,1, TimeUnit.MILLISECONDS);

        //EXECUTOR.scheduleAtFixedRate(() -> {
        //    String sql = "/*!nodeName=write_1;*/ select * from department where id = ?";
        //    Map<String, Object> result = jdbcTemplate.queryForObject(sql , new Object[] {1} ,new ColumnMapRowMapper());
        //    log.info("result {}" , result);
        //} , 1 ,1, TimeUnit.MILLISECONDS);

//        EXECUTOR.scheduleAtFixedRate(() -> {
//            String sql = "insert into employee (id , name , department_id) values (? , ? , ?)";
//            int row = jdbcTemplate.update(sql, ps -> {
//                ps.setLong(1 , new Random().nextLong());
//                ps.setString(2 , "He Xing Mo");
//                ps.setInt(3 , 1);
//            });
//            log.info("inserted row {}" , row);
//        } , 1 , 1 ,TimeUnit.MILLISECONDS);
//
//        EXECUTOR.scheduleAtFixedRate(() -> {
//            String sql = "update employee set name = ? , department_id = ? where id = ?";
//            int row = jdbcTemplate.update(sql, ps -> {
//                ps.setString(1 , "He Xing Mo_" + UUIDUtils.getSimpleUUID());
//                ps.setLong(2 , 1L);
//                ps.setInt(3 , 1);
//            });
//            log.info("updated row {}" , row);
//        } , 1 , 1 , TimeUnit.MILLISECONDS);
//
//        EXECUTOR.scheduleAtFixedRate(() -> {
//            String sql = "delete from employee where id = ?";
//            int row = jdbcTemplate.update(sql, new Random().nextLong());
//            log.info("deleted row {}" , row);
//        } , 1 ,1 , TimeUnit.MILLISECONDS);
    }
}
