package com.guddqs.mybatis.interceptor;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;

/**
 * @author 42784
 * @date 2016/9/28
 * @description seo-new
 */
public interface IDialectPlugin {
    /**
     * 替换链接字符串
     *
     * @param boundSql s
     */
    public void replaceConcat(BoundSql boundSql);

    /**
     * isnull函数的处理
     *
     * @param boundSql s
     */
    public void replaceIsnull(BoundSql boundSql);

    /**
     * 注入total sql
     *
     * @param oParams         s
     * @param boundSql        s
     * @param connection      s
     * @param mappedStatement s
     */
    public void setPageTotalSql(Object oParams, BoundSql boundSql, Connection connection, MappedStatement mappedStatement) throws Throwable;

    /**
     * 替换分页sql
     *
     * @param oParams  s
     * @param boundSql s
     */
    public void setPageSql(Object oParams, BoundSql boundSql) throws Throwable;
}
