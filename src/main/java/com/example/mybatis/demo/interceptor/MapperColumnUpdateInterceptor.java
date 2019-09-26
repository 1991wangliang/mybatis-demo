package com.example.mybatis.demo.interceptor;

import com.example.mybatis.demo.annotation.MapperColumn;
import com.example.mybatis.demo.domain.BaseMapperBean;
import com.example.mybatis.demo.domain.MapperThreadUserInfo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Date;
import java.util.List;


/**
 * @author lorne
 * @date 2019-09-26
 * @description 执行修改与插入sql的时候自动追加更新字段数据.
 */
@Slf4j
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class,Integer.class}))
public class MapperColumnUpdateInterceptor implements Interceptor {

    private final static String MAPPEDSTATEMENT_KEY = "delegate.mappedStatement";
    private final static String PARAMETEROBJECT_KEY = "parameterHandler.parameterObject";

    private  <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = realTarget(invocation.getTarget());
        MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue(MAPPEDSTATEMENT_KEY);
        Object parameterObject = metaStatementHandler.getValue(PARAMETEROBJECT_KEY);
        BoundSql boundSql = statementHandler.getBoundSql();
        SqlCommandType sqlCommandType =  mappedStatement.getSqlCommandType();

        //更新操作
        if(     SqlCommandType.UPDATE.equals(sqlCommandType)
                &&MapperThreadUserInfo.getInstance()!=null
                && haveMapperColumnOnObject(parameterObject)) {

            prepareSql(mappedStatement,metaStatementHandler,parameterObject,boundSql,sqlCommandType);
        }

        //插入操作
        if(     SqlCommandType.INSERT.equals(sqlCommandType)
                &&MapperThreadUserInfo.getInstance()!=null
                && haveMapperColumnOnObject(parameterObject)) {

            prepareSql(mappedStatement,metaStatementHandler,parameterObject,boundSql,sqlCommandType);
        }

        log.debug("update-boundSql->{}",boundSql.getSql());
        return invocation.proceed();
    }

    /**
     * 处理sql的更新业务
     * @param mappedStatement   mybatis MappedStatement
     * @param metaStatementHandler mybatis MetaObject
     * @param parameterObject   传入参数 bean
     * @param boundSql      sql对象
     * @param sqlCommandType    sql类型
     * @throws Exception    sql异常
     */
    private void prepareSql( MappedStatement mappedStatement,
                             MetaObject metaStatementHandler,
                             Object parameterObject,
                             BoundSql boundSql,
                             SqlCommandType sqlCommandType ) throws Exception {

        BaseMapperBean domain = (BaseMapperBean) parameterObject;
        String sql = boundSql.getSql();
        Statement statement = CCJSqlParserUtil.parse(sql);

        //通过反射获取bean对象
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(parameterObject.getClass());
        if (SqlCommandType.UPDATE.equals(sqlCommandType)) {

            domain.setLastUpdateTime(new Date());
            domain.setLastUpdateMan(MapperThreadUserInfo.getInstance().getUser());

            Update update = (Update) statement;

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                MapperColumn mapperColumn = propertyDescriptor.getReadMethod().getAnnotation(MapperColumn.class);
                if (mapperColumn != null
                        && mapperColumn.mode().equals(MapperColumn.EMapperColumn.UPDATE)) {
                    String columnName = mapperColumn.name();
                    addSqlColumn(mappedStatement.getConfiguration(), boundSql.getParameterMappings(),
                            update.getColumns(), update.getExpressions(), columnName, propertyDescriptor);
                }
            }
        }

        if (SqlCommandType.INSERT.equals(sqlCommandType)) {

            domain.setCreateTime(new Date());
            domain.setCreateMan(MapperThreadUserInfo.getInstance().getUser());

            Insert insert = (Insert) statement;

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                MapperColumn mapperColumn = propertyDescriptor.getReadMethod().getAnnotation(MapperColumn.class);
                if (mapperColumn != null
                        && mapperColumn.mode().equals(MapperColumn.EMapperColumn.INSERT)) {
                    String columnName = mapperColumn.name();
                    addSqlColumn(mappedStatement.getConfiguration(), boundSql.getParameterMappings(),
                            insert.getColumns(), ((ExpressionList) insert.getItemsList()).getExpressions(), columnName, propertyDescriptor);
                }
            }
        }

        //更新sql对象
        metaStatementHandler.setValue("delegate.boundSql.sql", statement.toString());
    }

    /**
     * 增加Sql参数
     * @param configuration mybatis configuration
     * @param parameterMappings mybatis parameterMappings 参数对象list
     * @param columns   SqlParser columns
     * @param expressions SqlParser expressions
     * @param columnName    字段名称
     * @param propertyDescriptor    java字段对象
     */
    private void addSqlColumn(Configuration configuration,
                              List<ParameterMapping> parameterMappings,
                              List<Column> columns,
                              List<Expression> expressions,
                              String columnName,
                              PropertyDescriptor propertyDescriptor){

        if (haveColumn(columns,columnName)) {
            columns.add(0, new Column(columnName));
            expressions.add(0, new JdbcParameter());

            if(!haveParameter(parameterMappings,propertyDescriptor.getName())) {
                ParameterMapping parameterMapping =
                        new ParameterMapping.Builder(
                                configuration, propertyDescriptor.getName(), propertyDescriptor.getPropertyType())
                                .build();
                parameterMappings.add(0, parameterMapping);
            }
        }
    }


    /**
     * 判断 mybatis.parameterMappings是否包含 propertyName
     * @param parameterMappings parameterMappings
     * @param propertyName propertyName
     * @return 是否包含
     */
    private boolean haveParameter(List<ParameterMapping> parameterMappings, String propertyName){
        for(ParameterMapping parameterMapping:parameterMappings){
            if(parameterMapping.getProperty().equals(propertyName)){
                return true;
            }
        }
        return  false;
    }

    /**
     * 判断 java.fields下是否存在MapperColumn注解
     * @param obj 判定对象
     * @return
     */
    private boolean haveMapperColumnOnObject(Object obj){
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(obj.getClass());
        for(PropertyDescriptor propertyDescriptor:propertyDescriptors){
            MapperColumn mapperColumn =  propertyDescriptor.getReadMethod().getAnnotation(MapperColumn.class);
            if(mapperColumn!=null){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断columnName 是否存在于 sqlparser.columns
     * @param columns sqlparser.columns
     * @param columnName 字段名称
     * @return
     */
    private boolean haveColumn(List<Column> columns, String columnName){
        for (Column column:columns){
            if(column.getColumnName().equals(columnName)){
                return true;
            }
        }
        return false;
    }
}
