package com.example.mybatis.demo.domain;

import com.example.mybatis.demo.annotation.MapperColumn;
import lombok.ToString;

import java.util.Date;

/**
 * @author lorne
 * @date 2019-09-26
 * @description 基层MapperBean,需要基于底层更新的domain必须继承此类
 */
@ToString
public class BaseMapperBean {


    /**
     * 创建者
     */

    private String createMan;

    /**
     * 数据库状态
     * 0 正常
     * 1 删除
     */
    private int state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新者
     */
    private String lastUpdateMan;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    @MapperColumn(name = "create_man")
    public String getCreateMan() {
        return createMan;
    }

    public void setCreateMan(String createMan) {
        this.createMan = createMan;
    }

    @MapperColumn(name = "state")
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @MapperColumn(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @MapperColumn(name = "last_update_man", mode = MapperColumn.EMapperColumn.UPDATE)
    public String getLastUpdateMan() {
        return lastUpdateMan;
    }

    public void setLastUpdateMan(String lastUpdateMan) {
        this.lastUpdateMan = lastUpdateMan;
    }

    @MapperColumn(name = "last_update_time", mode = MapperColumn.EMapperColumn.UPDATE)
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
