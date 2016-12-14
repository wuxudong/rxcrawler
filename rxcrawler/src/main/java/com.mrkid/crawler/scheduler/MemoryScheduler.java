package com.mrkid.crawler.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.DuplicateRemover;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 8:06 PM
 */
public class MemoryScheduler implements Scheduler {

    private DuplicateRemover duplicateRemover = request -> false;

    private Logger logger = LoggerFactory.getLogger(MemoryScheduler.class);

    private BlockingQueue<Request> queue = new LinkedBlockingQueue<>();

@Override
    public void restore() {
    }

    @Override
    public long size() {
        return queue.size();
    }

    @Override
    public void reset() {
        queue.clear();
    }


    @Override
    public void offer(Request request) {
        if (!duplicateRemover.isDuplicate(request)) {
            queue.offer(request);
        }


    }

    @Override
    public Request poll() {
        return queue.poll();
    }

    @Override
    public void finish(Request request) {
    }
}
