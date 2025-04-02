//package com.github.sqlx.integration.mybatis;
//
//import org.apache.ibatis.annotations.Insert;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//import org.apache.ibatis.annotations.Select;
//
//import java.util.Map;
//
//
///**
// * @author He Xing Mo
// * @since 1.0
// */
//
//@Mapper
//public interface AreaMapper {
//
//    @Insert("insert into area (id , name) values (#{val.id} , #{val.name})")
//    int insert(@Param("val") Map<String , Object> val);
//
//    @Insert("update area set name = #{val.name} where id = #{val.id}")
//    int updateById(@Param("val") Map<String , Object> val);
//
//    @Insert("delete from area where id = #{id}")
//    int deleteById(Long id);
//
//    @Select("select * from area where id = #{id}")
//    Map<String , Object> selectById(Long id);
//}
