package com.example.mybatis.demo.interceptor;


import com.example.mybatis.demo.MapperThreadUserInfo;
import com.example.mybatis.demo.annotation.UpdateColumn;
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
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 * @author lorne
 * @date 2019-09-26
 * @description 执行修改与插入sql的时候自动追加更新字段数据.
 */
@Slf4j
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class MapperUpdateColumnInterceptor implements Interceptor {

    private final static String MAPPEDSTATEMENT_KEY = "delegate.mappedStatement";


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
        BoundSql boundSql = statementHandler.getBoundSql();
        SqlCommandType sqlCommandType =  mappedStatement.getSqlCommandType();

        UpdateColumn updateColumn = getUpdateColumn(mappedStatement);

        //更新操作 param 对象
        if(     SqlCommandType.UPDATE.equals(sqlCommandType)
                && MapperThreadUserInfo.getInstance()!=null
                &&updateColumn!=null){

            prepareParamSql(mappedStatement,metaStatementHandler,boundSql,sqlCommandType,updateColumn);
        }

        //插入操作
        if(     SqlCommandType.INSERT.equals(sqlCommandType)
                &&MapperThreadUserInfo.getInstance()!=null
                &&updateColumn!=null) {

            prepareParamSql(mappedStatement,metaStatementHandler,boundSql,sqlCommandType,updateColumn);
        }

        log.debug("update-boundSql->{}",boundSql.getSql());
        return invocation.proceed();
    }


    /**
     * 处理sql的更新业务
     * @param mappedStatement   mybatis MappedStatement
     * @param metaStatementHandler mybatis MetaObject
     * @param boundSql      sql对象
     * @param sqlCommandType    sql类型
     * @param updateColumn   updateColumn
     * @throws Exception    sql异常
     */
    private void prepareParamSql( MappedStatement mappedStatement,
                                  MetaObject metaStatementHandler,
                                  BoundSql boundSql,
                                  SqlCommandType sqlCommandType,
                                  UpdateColumn updateColumn) throws Exception {

        String sql = boundSql.getSql();
        Statement statement = CCJSqlParserUtil.parse(sql);

        if (SqlCommandType.UPDATE.equals(sqlCommandType)) {

            Update update = (Update) statement;

            List<Column> columns = update.getColumns();
            List<Expression> expressions = update.getExpressions();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

            if (!haveColumn(update.getColumns(), updateColumn.lastUpdateMan())) {
                columns.add(0, new Column( updateColumn.lastUpdateMan()));
                expressions.add(0, new JdbcParameter());

                boundSql.setAdditionalParameter(updateColumn.lastUpdateMan(),MapperThreadUserInfo.getInstance().getUser());

                if(!haveParameter(parameterMappings,updateColumn.lastUpdateMan())) {
                    ParameterMapping parameterMapping =
                            new ParameterMapping.Builder(
                                    mappedStatement.getConfiguration(), updateColumn.lastUpdateMan(), String.class)
                                    .build();
                    parameterMappings.add(0, parameterMapping);
                }

            }

            if (!haveColumn(update.getColumns(), updateColumn.lastUpdateTime())) {
                columns.add(0, new Column( updateColumn.lastUpdateTime()));
                expressions.add(0, new JdbcParameter());

                boundSql.setAdditionalParameter(updateColumn.lastUpdateTime(),new Date());

                if(!haveParameter(parameterMappings,updateColumn.lastUpdateTime())) {
                    ParameterMapping parameterMapping =
                            new ParameterMapping.Builder(
                                    mappedStatement.getConfiguration(), updateColumn.lastUpdateTime(), Date.class)
                                    .build();
                    parameterMappings.add(0, parameterMapping);
                }
            }
        }

        if (SqlCommandType.INSERT.equals(sqlCommandType)) {

            Insert insert = (Insert) statement;

            List<Column> columns = insert.getColumns();
            List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

            if (!haveColumn(columns, updateColumn.createMan())) {
                columns.add(0, new Column( updateColumn.createMan()));
                expressions.add(0, new JdbcParameter());

                boundSql.setAdditionalParameter(updateColumn.createMan(),MapperThreadUserInfo.getInstance().getUser());

                if(!haveParameter(parameterMappings,updateColumn.createMan())) {
                    ParameterMapping parameterMapping =
                            new ParameterMapping.Builder(
                                    mappedStatement.getConfiguration(), updateColumn.createMan(), String.class)
                                    .build();
                    parameterMappings.add(0, parameterMapping);
                }

            }

            if (!haveColumn(columns, updateColumn.createTime())) {
                columns.add(0, new Column( updateColumn.createTime()));
                expressions.add(0, new JdbcParameter());

                boundSql.setAdditionalParameter(updateColumn.createTime(),new Date());

                if(!haveParameter(parameterMappings,updateColumn.createTime())) {
                    ParameterMapping parameterMapping =
                            new ParameterMapping.Builder(
                                    mappedStatement.getConfiguration(), updateColumn.createTime(), Date.class)
                                    .build();
                    parameterMappings.add(0, parameterMapping);
                }
            }
        }

        //更新sql对象
        metaStatementHandler.setValue("delegate.boundSql.sql", statement.toString());
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


    private UpdateColumn getUpdateColumn(MappedStatement mappedStatement) throws Exception{
        String id = mappedStatement.getId();
        String className = id.substring(0,id.lastIndexOf("."));
        String methodStr = id.substring(id.lastIndexOf(".")+1,id.length());
        Class clazz =  Class.forName(className);
        for (Method method: clazz.getMethods()){
            if(methodStr.equals(method.getName())){
                return method.getAnnotation(UpdateColumn.class);
            }
        }
        return null;
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

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
