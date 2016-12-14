package com.mrkid.crawler.pipeline;

import com.mrkid.crawler.RequestMatcher;
import com.mrkid.crawler.ResultItems;

public interface SubPipeline extends RequestMatcher {

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param resultItems resultItems
     * @return whether continue to match
     */
    MatchOther processResult(ResultItems resultItems) throws Exception;

}
