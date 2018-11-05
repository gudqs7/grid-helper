package com.guddqs.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2018/5/8
 */
public class PageEntity<T> {

    private List<T> data = new ArrayList<>();
    private int total;

    private Map<String, T> map;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Map<String, ?> getMap() {
        return map;
    }

    public void setMap(Map<String, T> map) {
        this.map = map;
    }
}
