package com.mrkid.ecommerce.crawler.httpasyncclient;

import com.sun.org.apache.regexp.internal.RE;
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


    public Request() {

    }

    public Request(String url) {
        this.url = url;
    }
}
