package com.mrkid.crawler.pipeline;

import com.mrkid.crawler.ResultItems;

/**
 * Pipeline is the persistent and offline process part of crawler.<br>
 * The interface Pipeline can be implemented to customize ways of persistent.
 */
public interface Pipeline {

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     */
    void process(ResultItems resultItems) throws Exception;
}
