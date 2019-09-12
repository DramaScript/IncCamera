package com.dramascript.dlibrary.http;

import java.io.Serializable;

/*
 * Cread By DramaScript on 2019/3/19
 */
public class BaseResponse implements Serializable {
    public String code;
    public String msg;
    public Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
