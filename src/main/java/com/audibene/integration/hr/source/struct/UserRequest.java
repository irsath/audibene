package com.audibene.integration.hr.source.struct;

import java.util.List;

public class UserRequest {
    private Integer code;
    private Meta meta;
    private List<User> data = null;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }

}