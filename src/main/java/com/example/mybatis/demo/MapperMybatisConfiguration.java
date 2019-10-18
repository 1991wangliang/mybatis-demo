package com.example.mybatis.demo;

import com.example.mybatis.demo.interceptor.MapperAllInterceptor;
import com.example.mybatis.demo.interceptor.MapperUpdateColumnInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lorne
 * @date 2019-09-26
 * @description
 */
@Configuration
public class MapperMybatisConfiguration {

//    @Bean
//    @ConditionalOnMissingBean
//    public MapperUpdateColumnInterceptor mapperUpdateColumnInterceptor(){
//        return new MapperUpdateColumnInterceptor();
//    }

    @Bean
    @ConditionalOnMissingBean
    public MapperAllInterceptor mapperAllInterceptor(){
        return new MapperAllInterceptor();
    }
}
