package com.example.mybatis.demo.annotation;

import java.lang.annotation.*;

/**
 * @author lorne
 * @date 2019-09-26
 * @description 识别映射关系注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MapperColumn {

    String name();

    EMapperColumn mode() default EMapperColumn.INSERT;

    static enum EMapperColumn{

        /**
         * 插入语句下使用
         */
        INSERT,
        /**
         * 更新语句下使用
         */
        UPDATE

    }

}
