package com.mrkid.crawler;

import lombok.Data;
import org.apache.http.HttpHost;

import java.util.HashMap;
import java.util.Map;

/**
 * Object contains setting for crawler.<br>
 */
@Data
public class Site {
    private String userAgent;

    // milliseconds
    private int sleepTime = 1000;

    private int retryTimes = 0;

    // milliseconds
    private int retrySleepTime = 1000;

    private int timeOut = 5000;

    private Map<String, String> headers = new HashMap<>();

    private HttpHost httpProxy;

    // max connection total
    private int maxConnTotal = 10;

    // max connections to single host
    private int maxConnPerRoute = 10;

}
