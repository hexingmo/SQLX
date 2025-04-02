//package com.github.sqlx.integration.mybatis;
//
//import org.apache.ibatis.annotations.Insert;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//
//import java.util.Map;
//
///**
// * @author He Xing Mo
// * @since 1.0
// */
//
//@Mapper
//public interface EmployeeMapper {
//
//    @Insert("insert into employee (id , name , department_id) values (#{val.id} , #{val.name} , #{val.departmentId})")
//    int insert(@Param("val") Map<String , Object> val);
//
//    @Insert("update employee set name = #{val.name} , department_id = #{val.departmentId} where id = #{val.id}")
//    int updateById(@Param("val") Map<String , Object> val);
//
//    @Insert("delete from employee where id = #{val.id}")
//    int deleteById(Long id);
//
//    Map<String , Object> selectById(Long id);
//}
