package com.example.mybatis.demo.annotation;

import java.lang.annotation.*;

/**
 * @author lorne
 * @date 2019-10-18
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UpdateColumn {

    String lastUpdateTime() default "last_update_time";

    String lastUpdateMan() default "last_update_man";

}
