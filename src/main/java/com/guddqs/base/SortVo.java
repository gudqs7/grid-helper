package com.guddqs.base;

/**
 * @author wq
 * @date 2018/5/10
 */
public class SortVo {

    public final static String DESC = "desc";

    private String field;
    private String direction;
    private String override;

    public SortVo() {
    }

    public SortVo(String field, String desc) {
        this.field = field;
        this.direction = desc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOverride() {
        return override;
    }

    public void setOverride(String override) {
        this.override = override;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
