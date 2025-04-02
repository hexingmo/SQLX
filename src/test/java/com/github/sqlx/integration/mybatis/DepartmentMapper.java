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
//public interface DepartmentMapper {
//
//    @Insert("insert into department (id , name , area_id) values (#{val.id} , #{val.name} , #{val.areaId})")
//    int insert(@Param("val") Map<String , Object> val);
//
//    @Insert("update department set name = #{val.name} , area_id = #{val.areaId} where id = #{val.id}")
//    int updateById(@Param("val") Map<String , Object> val);
//
//    @Insert("delete from department where id = #{val.id}")
//    int deleteById(Long id);
//
//    Map<String , Object> selectById(Long id);
//
//}
