package com.mrkid.crawler.processor;

import com.mrkid.crawler.Request;

public interface DuplicateRemover {
    boolean isDuplicate(Request request);
}
