package com.example.mybatis.demo.mapper;

import com.example.mybatis.demo.domain.Test;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author lorne
 * @date 2019-09-26
 * @description
 */
@Mapper
public interface TestMapper {

    @Insert("insert into t_test(name) values(#{name})")
//    @UpdateColumn
    int save(Test test);

    @Select("select * from t_test ")
    List<Test> findAll();

    @Update("update t_test set name = #{name} where id = #{id}")
//    @UpdateColumn
    int updateName(Test test);


    @Update("update `t_test` set name = #{name} where id = #{id}")
//    @UpdateColumn
    int updateNameById(@Param("name") String name,@Param("id") Long id);

    @Update("update t_test set name = 'name2' where id = 1 ")
    int updateOneName();

}
