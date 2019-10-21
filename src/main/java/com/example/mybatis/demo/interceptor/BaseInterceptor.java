package com.example.mybatis.demo.interceptor;

import com.example.mybatis.demo.MapperThreadUserInfo;
import com.example.mybatis.demo.annotation.UpdateColumn;
import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.schema.Column;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

/**
 * @author lorne
 * @date 2019-10-21
 * @description
 */
abstract class BaseInterceptor implements Interceptor {

    protected final static String MAPPEDSTATEMENT_KEY = "delegate.mappedStatement";


    protected   <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }


    /**
     * 判断 mybatis.parameterMappings是否包含 propertyName
     * @param parameterMappings parameterMappings
     * @param propertyName propertyName
     * @return 是否包含
     */
    protected boolean haveParameter(List<ParameterMapping> parameterMappings, String propertyName){
        for(ParameterMapping parameterMapping:parameterMappings){
            if(parameterMapping.getProperty().equals(propertyName)){
                return true;
            }
        }
        return  false;
    }


    /**
     * 判断columnName 是否存在于 sqlparser.columns
     * @param columns sqlparser.columns
     * @param columnName 字段名称
     * @return
     */
    protected boolean haveColumn(List<Column> columns, String columnName){
        for (Column column:columns){
            if(column.getColumnName().equals(columnName)){
                return true;
            }
        }
        return false;
    }

    protected void checkParamter(List<Column> columns,
                               List<Expression> expressions,
                               List<ParameterMapping> parameterMappings,
                               BoundSql boundSql,
                               MappedStatement mappedStatement,
                               String columnName,Class type,Object val){
        if (!haveColumn(columns,columnName)) {
            columns.add(0, new Column( columnName));
            expressions.add(0, new JdbcParameter());

            boundSql.setAdditionalParameter(columnName, val);

            if(!haveParameter(parameterMappings,columnName)) {
                ParameterMapping parameterMapping =
                        new ParameterMapping.Builder(
                                mappedStatement.getConfiguration(), columnName, type)
                                .build();
                parameterMappings.add(0, parameterMapping);
            }
        }
    }
}
