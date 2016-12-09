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

    private int sleepTime = 5000;

    private int retryTimes = 0;

    private int retrySleepTime = 1000;

    private int timeOut = 5000;

    private Map<String, String> headers = new HashMap<String, String>();

    private HttpHost httpProxy;

}
