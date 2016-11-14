package com.mrkid.ecommerce.crawler.webmagic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Use Redis as url scheduler for distributed crawlers.
 * Support POST.
 * It is lack of DuplicateRemover function to save memory. (It is common to crawl api).
 * It store processing request to redis, and remove it when the request is finished, so we won't lose any requests.
 *
 * @author xudong82113@gmail.com <br>
 * @since 0.2.0
 */
public class DummyRedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover,
        SpiderListener {

    private static final String PENDING_QUEUE_KEY = "crawler_pending_queue";

    private static final String PROCESSING_QUEUE_KEY = "crawler_processing_queue";

    private ObjectMapper objectMapper = new ObjectMapper();

    // a field in request extras, the unique id of request
    private static String REQUEST_UUID = "REQUEST_UUID";

    // a field in request extras, the unique id of request's owner task
    private static String TASK_UUID = "TASK_UUID";

    private AtomicBoolean inited = new AtomicBoolean(false);

    private final StringRedisTemplate redisTemplate;

    public DummyRedisScheduler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        setDuplicateRemover(this);
    }

    private void init(Task task) {
        final BoundHashOperations<String, String, String> operations = redisTemplate
                .boundHashOps(getProcessingQueueKey());

        final Map<String, String> map = operations.entries();

        redisTemplate.delete(getProcessingQueueKey());

        for (String line : map.values()) {
            redisTemplate.boundListOps(getPendingQueueKey()).rightPush(line);
        }

        inited.set(true);
        logger.info("init cache scheduler success");
    }


    @Override
    protected void pushWhenNoDuplicate(Request request, Task task) {
        if (!inited.get()) {
            init(task);
        }

        request.putExtra(REQUEST_UUID, UUID.randomUUID().toString());
        request.putExtra(TASK_UUID, task.getUUID());

        try {
            redisTemplate.boundListOps(getPendingQueueKey()).rightPush(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
        }
    }

    @Override
    public synchronized Request poll(Task task) {
        if (!inited.get()) {
            init(task);
        }

        try {

            String line = redisTemplate.boundListOps(getPendingQueueKey()).leftPop();
            if (line == null) {
                return null;
            }

            Request request = objectMapper.readValue(line, Request.class);
            redisTemplate.boundHashOps(getProcessingQueueKey())
                    .put(request.getExtra(REQUEST_UUID), line);


            // HttpDownloader require nameValuePair as NameValuePair[],
            // but jackson will default deserialize it as List<Map<String, String>>
            final Object list = request.getExtra("nameValuePair");
            if (list != null && list instanceof List) {
                request.putExtra("nameValuePair",
                        ((List<Map<String, String>>) list).stream()
                                .map(m -> new BasicNameValuePair(m.get("name"), m.get("value"))
                                ).collect(Collectors.toList()).toArray(new NameValuePair[0]));
            }

            return request;
        } catch (IOException e) {
            return null;
        }
    }


    protected String getPendingQueueKey() {
        return PENDING_QUEUE_KEY;
    }

    protected String getProcessingQueueKey() {
        return PROCESSING_QUEUE_KEY;
    }



    @Override
    public int getLeftRequestsCount(Task task) {
        Long size = redisTemplate.boundHashOps(getPendingQueueKey()).size();
        return size.intValue();
    }

    @Override
    public void resetDuplicateCheck(Task task) {
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
        if (!inited.get()) {
            init(task);
        }

        return false;
    }


    // totalCount is ignored to save memory
    @Override
    public int getTotalRequestsCount(Task task) {
        return 0;
    }

    @Override
    public void onSuccess(Request request) {
        finishProcessing(request);
    }

    @Override
    public void onError(Request request) {
        finishProcessing(request);
    }

    private void finishProcessing(Request request) {
        final String taskUUID = (String) request.getExtra(TASK_UUID);
        if (taskUUID != null) {
            redisTemplate.boundHashOps(getProcessingQueueKey()).delete(request.getExtra(REQUEST_UUID));
        }
    }

    public void clearAll() {
        redisTemplate.delete(getProcessingQueueKey());
        redisTemplate.delete(getPendingQueueKey());

    }



}
