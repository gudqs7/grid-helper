package com.guddqs.base;

import com.guddqs.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 * @date 2018/5/10
 */
public class ParamVo {

    private Integer pageNo = 1;
    private Integer pageSize = 20;
    private List<FilterVo> filter = new ArrayList<>();
    private List<SortVo> sort = new ArrayList<>();
    private MapBean other = new MapBean();

    public ParamVo() {
    }

    public ParamVo(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public ParamVo(Integer pageNo, Integer pageSize) {
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
    }

    public int getStart() {
        return (pageNo - 1) * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }

    public MapBean getOther() {
        return other;
    }

    public void setOther(MapBean other) {
        this.other = other;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        this.pageSize = pageSize;
    }

    public List<FilterVo> getFilter() {
        return filter;
    }

    public void setFilter(List<FilterVo> filter) {
        this.filter.clear();
        for (FilterVo filterVo : filter) {
            addFilter(filterVo);
        }
    }

    public ParamVo addFilter(FilterVo filterVo) {
        String type = filterVo.getType();
        Object value = filterVo.getValue();
        filterVo.setRight(1);
        filterVo.setValue0(value);
        if (StringUtil.isEmpty(type)) {
            filterVo.setRight(0);
            return this;
        }
        if (FilterVo.Type.STRING.equals(type)) {
            filterVo.setOperator(" like ");
            filterVo.setValue0("'%" + value + "%'");
        }
        if (FilterVo.Type.DATE.equals(type)) {
            filterVo.setValue0("'" + value + "'");
        }
        if (FilterVo.Type.NUMERIC.equals(type) || FilterVo.Type.DATE.equals(type)) {
            String comparison = filterVo.getOperator();
            if (FilterVo.Comparison.LESS_THEN.equals(comparison)) {
                filterVo.setOperator(" < ");
            }
            if (FilterVo.Comparison.GREAT_THEN.equals(comparison)) {
                filterVo.setOperator(" > ");
            }
            if (FilterVo.Comparison.LESS_AND_EQUALS.equals(comparison)) {
                filterVo.setOperator(" <= ");
            }
            if (FilterVo.Comparison.GREAT_AND_EQUALS.equals(comparison)) {
                filterVo.setOperator(" >= ");
            }
            if (FilterVo.Comparison.EQUALS.equals(comparison)) {
                if (FilterVo.Type.NUMERIC.equals(type)) {
                    filterVo.setOperator(" = ");
                } else {
                    filterVo.setOperator(" like ");
                    filterVo.setValue0("'%" + value + "%'");
                }
            }
            if (StringUtil.isEmpty(value.toString())) {
                filterVo.setRight(0);
            }

        }
        if (FilterVo.Type.LIST.equals(type)) {
            filterVo.setOperator(" in ");
            StringBuilder sbValue = new StringBuilder("(");
            if (filterVo.getValue() != null) {
                if (filterVo.getValue() instanceof List) {
                    List valList = (List) filterVo.getValue();
                    for (int j = 0; j < valList.size(); j++) {
                        Object val = valList.get(j);
                        if (val instanceof Integer || val instanceof Float || val instanceof Double) {
                            sbValue.append(val.toString());
                        } else {
                            sbValue.append("'");
                            sbValue.append(val.toString());
                            sbValue.append("'");
                        }
                        if (j < valList.size() - 1) {
                            sbValue.append(",");
                        }
                    }
                    if (valList.size() > 0) {
                        filterVo.setValue0(sbValue.toString() + ")");
                    } else {
                        filterVo.setRight(0);
                    }
                } else {
                    filterVo.setRight(0);
                }
            }
        }
        this.filter.add(filterVo);
        return this;
    }

    public List<SortVo> getSort() {
        return sort;
    }

    public void setSort(List<SortVo> sort) {
        this.sort = sort;
    }

    public ParamVo desc(String field) {
        this.getSort().add(new SortVo(field, "DESC"));
        return this;
    }

    public void asc(String field) {
        this.getSort().add(new SortVo(field, "ASC"));
    }
}
