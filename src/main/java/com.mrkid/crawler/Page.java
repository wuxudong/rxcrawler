package com.mrkid.crawler;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Page {

    private static final Logger logger = LoggerFactory.getLogger(Page.class);

    private final Request request;

    private String rawText;

    private List<Request> targetRequests = new ArrayList<Request>();

    private Map<String, Object> context = new HashMap<>();

    private final ResultItems resultItems;

    public void addTargetRequest(Request request) {
        targetRequests.add(request);
    }

    public Page(Request request) {
        this.request = request;
        this.resultItems = new ResultItems(request);
    }

}