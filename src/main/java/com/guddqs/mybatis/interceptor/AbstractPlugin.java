package com.guddqs.mybatis.interceptor;

import com.guddqs.base.FilterVo;
import com.guddqs.base.PageEntity;
import com.guddqs.base.ParamVo;
import com.guddqs.base.SortVo;
import com.guddqs.mybatis.ReflectUtil;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 42784
 * @date 2016/9/28
 * @description seo-new
 */
public abstract class AbstractPlugin implements IDialectPlugin {

    private static Pattern tablePre = Pattern.compile("(isnull\\(.*?\\)|ifnull\\(.*?\\)|nvl\\(.*?\\))", Pattern.CASE_INSENSITIVE);

    @Override
    public void replaceConcat(BoundSql boundSql) {
    }

    @Override
    public void replaceIsnull(BoundSql boundSql) {
        String sql = boundSql.getSql();

        Matcher m = tablePre.matcher(sql);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String matcher = m.group(1);
            String result = matcher.replace("isnull", "COALESCE").replace("ifnull", "COALESCE").replace("nvl", "COALESCE");
            m.appendReplacement(sb, result);
        }
        m.appendTail(sb);

        ReflectUtil.setFieldValue(boundSql, "sql", sb.toString());
    }

    @Override
    public void setPageTotalSql(Object oParams, BoundSql boundSql, Connection connection, MappedStatement mappedStatement) throws Throwable {
        @SuppressWarnings("unchecked")
        Map<String, Object> pMap = (Map<String, Object>) oParams;
        ParamVo gridParams = (ParamVo) (pMap.get("paramVo"));

        StringBuffer sbSql = new StringBuffer();
        sbSql.append("select * from ( ");
        sbSql.append(boundSql.getSql());
        sbSql.append(") t_ ");

        this.appendFilterSql(sbSql, gridParams.getFilter());

        ReflectUtil.setFieldValue(boundSql, "sql", sbSql.toString());

        if (gridParams.getLimit() <= 0) {
            return;
        }

        StringBuffer sbCountSql = new StringBuffer();
        sbCountSql.append("SELECT COUNT(0) FROM ( ");
        sbCountSql.append(sbSql.toString());
        sbCountSql.append(") tmp ");
        String countSql = sbCountSql.toString();
        PreparedStatement countStmt = connection.prepareStatement(countSql);
        setParameters(countStmt, mappedStatement, boundSql, oParams);
        ResultSet rs = countStmt.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        countStmt.close();

        PageEntity pageMode = (PageEntity) (pMap.get("pageEntity"));
        pageMode.setTotal(count);
    }

    @Override
    public void setPageSql(Object oParams, BoundSql boundSql) throws Throwable {
    }

    private void appendFilterSql(StringBuffer sbSql, List<FilterVo> filterList) {
        StringBuilder sbFilter = new StringBuilder();
        for (FilterVo e : filterList) {
            if (e.getRight() == 1) {
                sbFilter.append(" and ");
                sbFilter.append(e.getField());
                sbFilter.append(e.getOperator());
                sbFilter.append(e.getValue0());
            }
        }
        if (sbFilter.length() > 0) {
            sbSql.append(" where 1 = 1 ");
            sbSql.append(sbFilter);
        }
    }

    protected String getSoreSql(List<SortVo> sortList) {
        StringBuilder sbOrder = new StringBuilder();
        if (sortList.size() > 0) {
            sbOrder.append(" order by ");
            for (int i = 0; i < sortList.size(); i++) {
                SortVo e = sortList.get(i);
                sbOrder.append(e.getField());
                sbOrder.append(" ");
                sbOrder.append(e.getDirection());
                if (i < sortList.size() - 1) {
                    sbOrder.append(", ");
                }
            }
        }
        return sbOrder.toString();
    }

    private void setParameters(PreparedStatement ps,
                               MappedStatement mappedStatement, BoundSql boundSql,
                               Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters")
                .object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql
                .getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration
                    .getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null
                    : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry
                            .hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName
                            .startsWith(ForEachSqlNode.ITEM_PREFIX)
                            && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value)
                                    .getValue(
                                            propertyName.substring(prop
                                                    .getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject
                                .getValue(propertyName);
                    }
                    @SuppressWarnings("unchecked")
                    TypeHandler<Object> typeHandler = (TypeHandler<Object>) parameterMapping
                            .getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException(
                                "There was no TypeHandler found for parameter "
                                        + propertyName + " of statement "
                                        + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value,
                            parameterMapping.getJdbcType());
                }
            }
        }
    }
}
