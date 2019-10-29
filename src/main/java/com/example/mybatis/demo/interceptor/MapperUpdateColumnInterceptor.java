package com.example.mybatis.demo.interceptor;


import com.example.mybatis.demo.MapperThreadUserInfo;
import com.example.mybatis.demo.annotation.UpdateColumn;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author lorne
 * @date 2019-09-26
 * @description 执行修改与插入sql的时候自动追加更新字段数据.
 */
@Slf4j
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class MapperUpdateColumnInterceptor extends BaseInterceptor implements Interceptor {


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

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if(parameterMappings==null||parameterMappings.size()==0){
            parameterMappings = new ArrayList<>();
        }

        if (SqlCommandType.UPDATE.equals(sqlCommandType)) {

            Update update = (Update) statement;

            List<Column> columns = update.getColumns();
            List<Expression> expressions = update.getExpressions();

            checkParamter(columns,expressions,parameterMappings,boundSql,mappedStatement, updateColumn.lastUpdateMan(),String.class,MapperThreadUserInfo.getInstance().getUser());

            checkParamter(columns,expressions,parameterMappings,boundSql,mappedStatement,updateColumn.lastUpdateTime(),Date.class,new Date());

        }

        if (SqlCommandType.INSERT.equals(sqlCommandType)) {

            Insert insert = (Insert) statement;

            List<Column> columns = insert.getColumns();
            List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();

            checkParamter(columns,expressions,parameterMappings,boundSql,mappedStatement, updateColumn.createMan(),String.class,MapperThreadUserInfo.getInstance().getUser());

            checkParamter(columns,expressions,parameterMappings,boundSql,mappedStatement,updateColumn.createTime(),Date.class,new Date());

        }

        //更新sql对象
        metaStatementHandler.setValue("delegate.boundSql.sql", statement.toString());
        metaStatementHandler.setValue("delegate.boundSql.parameterMappings", parameterMappings);
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

}
