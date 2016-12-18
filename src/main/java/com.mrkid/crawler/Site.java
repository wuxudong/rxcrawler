package com.mrkid.crawler;

import lombok.Data;
import org.apache.http.HttpHost;

import java.util.HashMap;
import java.util.Map;

/**
 * Object contains setting for crawler.
 */
@Data
public class Site {
    private String userAgent;

    /**
     * Interval between the processing of two pages.
     * Time unit is milliseconds.<br>
     */

    private int sleepTime = 0;

    /**
     * Retry times when download fail, 0 by default.
     */
    private int retryTimes = 0;

    /**
     * Retry sleep times when download fail, 1000 by default.
     */
    private int retrySleepTime = 0;

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: {@code -1}
     * </p>
     */
    private int connectionTimeOut = -1;

    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: {@code -1}
     * </p>
     */
    private int socketTimeOut = -1;


    /**
     * Defines the request timeout in milliseconds,
     * which includes establishing connection, transfer data.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * </p>
     * <p>
     * Default: {@code 0}
     * </p>
     */
    private int overallTimeout = 0;

    private Map<String, String> headers = new HashMap<>();

    private HttpHost httpProxy;

    // max connection total
    private int maxConnTotal = 100;

    // max connections to single host
    private int maxConnPerRoute = 100;

}
