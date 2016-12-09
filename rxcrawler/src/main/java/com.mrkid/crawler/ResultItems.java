package com.mrkid.crawler;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Object contains extract results.<br>
 * It is contained in Page and will be processed in pipeline.
 */
@Data
public class ResultItems {
    private final Map<String, Object> fields = new LinkedHashMap<String, Object>();

    private final Request request;

    public <T> T get(String key) {
        Object o = fields.get(key);
        if (o == null) {
            return null;
        }
        return (T) fields.get(key);
    }

    public <T> void put(String key, T value) {
        fields.put(key, value);
    }
}
