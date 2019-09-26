package com.example.mybatis.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author lorne
 * @date 2019-09-26
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper=true)
public class Test extends BaseMapperBean {

    private Long id;

    private String name;
}
