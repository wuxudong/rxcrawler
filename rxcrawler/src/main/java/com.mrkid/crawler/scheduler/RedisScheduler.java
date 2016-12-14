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
import java.util.concurrent.TimeUnit;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 8:06 PM
 */
public class RedisScheduler implements Scheduler {

    private static final String PENDING_QUEUE_KEY = "crawler_pending_queue";

    private static final String PROCESSING_QUEUE_KEY = "crawler_processing_queue";

    private ObjectMapper objectMapper = new ObjectMapper();

    // a field in request extras, the unique id of request
    private static final String REQUEST_UUID = "REQUEST_UUID";

    private StringRedisTemplate redisTemplate;

    private DuplicateRemover duplicateRemover = request -> false;

    private Logger logger = LoggerFactory.getLogger(RedisScheduler.class);

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * It should only be called at beginning, not thread safe
     * restore crawling request killed in the last run
     */
    @Override
    public void restore() {
        // TODO make operation atomic
        final Map<String, String> processing = redisTemplate
                .<String, String>boundHashOps(getProcessingQueueKey()).entries();

        final List<String> pending = redisTemplate.boundListOps(getPendingQueueKey()).range(0l, -1l);

        reset();

        // use uuid for unique
        final Map<String, String> map = new LinkedHashMap<>();

        for (String s : pending) {
            try {
                Request request = objectMapper.readValue(s, Request.class);
                map.put(getRequestUUID(request), s);
            } catch (IOException e) {
                logger.error("fail to parse Request:" + s);
            }
        }

        processing.entrySet().forEach(entry -> map.putIfAbsent(entry.getKey(), entry.getValue()));

        for (String line : map.values()) {
            redisTemplate.boundListOps(getPendingQueueKey()).rightPush(line);
        }

    }

    @Override
    public long size() {
        return redisTemplate.boundListOps(getPendingQueueKey()).size();
    }


    private void assignRequestUUID(Request request) {
        if (StringUtils.isBlank(getRequestUUID(request))) {
            request.putExtra(REQUEST_UUID, UUID.randomUUID().toString());
        }
    }


    private String getRequestUUID(Request request) {
        return (String) request.getExtra(REQUEST_UUID);
    }

    protected String getPendingQueueKey() {
        return PENDING_QUEUE_KEY;
    }

    protected String getProcessingQueueKey() {
        return PROCESSING_QUEUE_KEY;
    }

    @Override
    public void reset() {
        redisTemplate.delete(getProcessingQueueKey());
        redisTemplate.delete(getPendingQueueKey());
    }


    @Override
    public void offer(Request request) {
        if (!duplicateRemover.isDuplicate(request)) {
            assignRequestUUID(request);

            try {
                redisTemplate.boundListOps(getPendingQueueKey()).rightPush(objectMapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
            }
        }


    }

    @Override
    public Request poll() {
        try {

            String line = redisTemplate.boundListOps(getPendingQueueKey()).leftPop();
            if (line == null) {
                return null;
            }

            Request request = objectMapper.readValue(line, Request.class);
            redisTemplate.boundHashOps(getProcessingQueueKey())
                    .put(getRequestUUID(request), line);


            return request;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void finish(Request request) {
        redisTemplate.boundHashOps(getProcessingQueueKey()).delete(getRequestUUID(request));
    }
}
