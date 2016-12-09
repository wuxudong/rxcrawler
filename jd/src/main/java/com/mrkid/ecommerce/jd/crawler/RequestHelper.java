package com.mrkid.ecommerce.jd.crawler;

import com.mrkid.crawler.Request;
import com.mrkid.ecommerce.jd.dto.JDCategoryDTO;
import com.mrkid.ecommerce.jd.utils.JDUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 6:43 PM
 */
public class RequestHelper {

    public static boolean shouldFetchItems(JDCategoryDTO category) {
        String path = category.getPath();

        if (StringUtils.isBlank(path)) {
            return false;
        }

        final String[] tokens = path.split("_");

        if (tokens.length != 3) {
            return false;
        }

        long rootCid = Long.valueOf(tokens[0]);
        return JDUtils.shouldFetchItems(rootCid);
    }

    public static Request topCategoriesRequest() {
        final Request request = new Request("http://so.m.jd.com/category/all.html");
        request.setPageType(PageType.TOP_CATEGORY);
        return request;
    }


    public static Request subCategoriesRequest(long cid) {
        String url = "http://so.m.jd.com/category/list.action?_format_=json&catelogyId=" + cid;
        Request request = new Request(url);
        request.setPageType(PageType.SUB_CATEGORY);
        return request;
    }

    /**
     * @param category
     * @param page
     * @return null if the category should be skipped
     */
    public static Request listRequest(JDCategoryDTO category, int page) {
        if (!shouldFetchItems(category)) {
            return null;
        }

        Map<String, String> form = new HashMap<>();

        form.put("_format_", "json");
        form.put("stock", "1");

        String path = category.getPath();
        final String[] tokens = path.split("_");

        form.put("c1", tokens[0]);
        form.put("c2", tokens[1]);
        form.put("categoryId", tokens[2]);
        form.put("page", String.valueOf(page));

        final Request request = new Request("http://so.m.jd.com/ware/searchList.action");
        request.setMethod("POST");
        request.setForm(form);
        request.getExtras().put("category", category);

        request.setPageType(PageType.LIST);

        return request;
    }
}
