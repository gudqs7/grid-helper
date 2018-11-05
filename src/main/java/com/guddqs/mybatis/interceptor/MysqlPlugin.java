package com.guddqs.mybatis.interceptor;

import com.guddqs.base.ParamVo;
import com.guddqs.mybatis.ReflectUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;
import java.util.Map;

/**
 * @author 42784
 * @date 2016/9/28
 * @description seo-new
 */
public class MysqlPlugin extends AbstractPlugin implements IDialectPlugin {

    @Override
    public void setPageTotalSql(Object oParams, BoundSql boundSql, Connection connection, MappedStatement mappedStatement) throws Throwable {
        super.setPageTotalSql(oParams,boundSql,connection,mappedStatement);
    }

    @Override
    public void setPageSql(Object oParams, BoundSql boundSql) throws Throwable {

        @SuppressWarnings("unchecked")
        Map<String,Object> pMap = (Map<String,Object>)oParams;
        ParamVo gridParams = (ParamVo)(pMap.get("paramVo"));

        String orderSql = getSoreSql(gridParams.getSort());

        String sql = boundSql.getSql();
        StringBuilder pagingSelect = new StringBuilder();
        pagingSelect.append("select * from ( ");
        pagingSelect.append(sql);
        pagingSelect.append(" ) _tableTmp ");
        pagingSelect.append(orderSql);
        if (gridParams.getLimit() > 0) {
            pagingSelect.append(" limit ");
            pagingSelect.append(gridParams.getStart());
            pagingSelect.append(",");
            pagingSelect.append(gridParams.getLimit());
        }

        ReflectUtil.setFieldValue(boundSql, "sql", pagingSelect.toString());
    }
}
