package com.mrkid.crawler.scheduler;

import com.mrkid.crawler.Request;

public interface Scheduler {
    /**
     * add a url to fetch
     * @param request request
     */
    void offer(Request request);

    /**
     * get an url to crawl, then mark it as processing
     * @return the url to crawl
     */
    Request poll();

    /**
     * mark the request as finished
     * @param request
     */
    void finish(Request request);

    /**
     * clear all requests
     */
    void reset();

    /**
     * restore from last unexpected termination
     */
    void restore();

}
