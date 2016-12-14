package com.mrkid.crawler;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 7:15 PM
 */
public class CrawlerException extends Exception {
    public CrawlerException(String message) {
        super(message);
    }

    public CrawlerException(Throwable cause) {
        super(cause);
    }
}
