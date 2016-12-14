package com.mrkid.crawler.processor;

import com.mrkid.crawler.Page;

public interface PageProcessor {

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page page
     */
    Page process(Page page) throws Exception;
}
