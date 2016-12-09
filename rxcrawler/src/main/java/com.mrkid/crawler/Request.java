package com.mrkid.crawler;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Request implements Serializable {

    private String url;

    private String method = "GET";

    private Map<String, String> form = new HashMap<>();

    private Integer pageType;

    private Map<String, Object> extras = new HashMap<>();

    public Request() {
    }

    public Request(String url) {
        this.url = url;
    }

    public Object getExtra(String key) {
        return extras.get(key);
    }

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }
}
