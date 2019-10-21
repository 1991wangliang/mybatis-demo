package com.example.mybatis.demo;

import com.example.mybatis.demo.mapper.TestMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.reflect.misc.MethodUtil;

import java.lang.reflect.Method;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DemoApplicationTests {


    @Autowired
    private TestMapper testMapper;


    @Before
    public void init(){
        //模拟切面先将用户信息传递到ThreadLocal下
        MapperThreadUserInfo mapperThreadUserInfo = new MapperThreadUserInfo();
        mapperThreadUserInfo.setUser("xiao li");
        MapperThreadUserInfo.set(mapperThreadUserInfo);
    }

    @Test
    public void update() {
        com.example.mybatis.demo.domain.Test test = new com.example.mybatis.demo.domain.Test();
        test.setName("new Name 1");
        test.setId(1L);
        int rs = testMapper.updateName(test);
        log.info("rs->{}",rs);
        Assert.assertTrue(rs==1);

        List<com.example.mybatis.demo.domain.Test> list =  testMapper.findAll();
        log.info("list->{}",list);
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void updateNameById() {
        int rs = testMapper.updateNameById("my test name",2L);
        log.info("rs->{}",rs);
        Assert.assertTrue(rs==1);

        List<com.example.mybatis.demo.domain.Test> list =  testMapper.findAll();
        log.info("list->{}",list);
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void insert() {
        com.example.mybatis.demo.domain.Test test1 = new com.example.mybatis.demo.domain.Test();
        test1.setName("my name 1");
        testMapper.save(test1);
        test1.setName("my name 2");
        int rs = testMapper.save(test1);
        Assert.assertTrue(rs==1);
        log.info("rs->{}",rs);

        List<com.example.mybatis.demo.domain.Test> list =  testMapper.findAll();
        log.info("list->{}",list);
        Assert.assertTrue(list.size()>0);
    }


}
