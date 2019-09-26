package com.example.mybatis.demo.domain;

/**
 * @author lorne
 * @date 2019-09-26
 * @description  传递用户ThreadLocal对象
 */
public class MapperThreadUserInfo {

    private static ThreadLocal<MapperThreadUserInfo> threadLocal = new ThreadLocal<>();

    public static MapperThreadUserInfo getInstance() {
        return threadLocal.get();
    }

    public static void set(MapperThreadUserInfo mapperThreadUserInfo) {
        MapperThreadUserInfo.threadLocal .set(mapperThreadUserInfo);
    }

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
