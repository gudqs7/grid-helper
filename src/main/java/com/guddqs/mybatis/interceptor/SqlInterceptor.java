package com.guddqs.mybatis.interceptor;

import com.guddqs.mybatis.ReflectUtil;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.CallableStatementHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wq
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlInterceptor implements Interceptor {

    private static final String ORACLE = "oracle.jdbc.driver.OracleDriver";
    private static final String MYSQL = "com.mysql.jdbc.Driver";
    private static final String MARIADB = "org.mariadb.jdbc.Driver";
    private static final String MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Value("spring.datasource.driver-class-name")
    private String dialect = MYSQL;

    private IDialectPlugin dialectPlugin = new MysqlPlugin();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        RoutingStatementHandler statement = (RoutingStatementHandler) invocation.getTarget();
        if (ReflectUtil.getFieldValue(statement, "delegate").getClass().toString().equals(CallableStatementHandler.class.toString())) {
            return invocation.proceed();
        }

        BoundSql boundSql = statement.getBoundSql();

        this.dialectPlugin.replaceConcat(boundSql);
        this.dialectPlugin.replaceIsnull(boundSql);

        PreparedStatementHandler handler = (PreparedStatementHandler) ReflectUtil.getFieldValue(statement, "delegate");

        ParameterHandler parameters = (ParameterHandler) ReflectUtil.getFieldValue(handler, "parameterHandler");
        Object oParams = parameters.getParameterObject();

        Map<String, Object> pMap = new HashMap<String, Object>();
        if (oParams instanceof Map<?, ?>) {
            pMap = (Map<String, Object>) oParams;
        }

        if (pMap.containsKey("paramVo") && pMap.containsKey("pageEntity")) {
            Connection connection = (Connection) invocation.getArgs()[0];
            MappedStatement mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(handler, "mappedStatement");

            this.dialectPlugin.setPageTotalSql(oParams, boundSql, connection, mappedStatement);
            this.dialectPlugin.setPageSql(oParams, boundSql);
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }


}