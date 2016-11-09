package com.mrkid.ecommerce.crawler.webmagic;

import org.apache.http.NameValuePair;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.util.*;

/**
 * User: xudong
 * Date: 09/11/2016
 * Time: 9:55 AM
 */
public class StrictHashSetDuplicateRemover implements DuplicateRemover {

    // binary is not supported
    private Map<String, Map<String, Set<List<NameValuePair>>>> requests = new HashMap<>();

    @Override
    public synchronized boolean isDuplicate(Request request, Task task) {
        final NameValuePair[] nameValuePairArray = (NameValuePair[]) request.getExtra("nameValuePair");

        List<NameValuePair> nameValuePairList =
                nameValuePairArray == null ? new ArrayList<>() : Arrays.asList(nameValuePairArray);

        final String url = getUrl(request);
        Map<String, Set<List<NameValuePair>>> map = requests.get(url);

        if (map == null) {
            map = new HashMap<>();
            requests.put(url, map);
        }

        final String method = request.getMethod() == null ? "GET" : request.getMethod();
        Set<List<NameValuePair>> set = map.get(method);

        if (set == null) {
            set = new HashSet<>();
            map.put(method, set);
        }

        return !set.add(nameValuePairList);
    }

    protected String getUrl(Request request) {
        return request.getUrl();
    }

    @Override
    public synchronized void resetDuplicateCheck(Task task) {
        requests.clear();
    }

    @Override
    public synchronized int getTotalRequestsCount(Task task) {
        return requests.values().stream().flatMap(m -> m.values().stream()).mapToInt(s -> s.size()).sum();
    }
}