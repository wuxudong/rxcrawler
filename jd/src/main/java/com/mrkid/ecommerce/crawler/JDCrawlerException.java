package com.mrkid.ecommerce.crawler;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:43 PM
 */
public class JDCrawlerException extends Exception {
    public JDCrawlerException(String message) {
        super(message);
    }

    public JDCrawlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JDCrawlerException(Throwable cause) {
        super(cause);
    }
}
