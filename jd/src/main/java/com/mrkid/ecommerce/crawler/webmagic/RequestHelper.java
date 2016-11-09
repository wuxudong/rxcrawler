package com.mrkid.ecommerce.crawler.webmagic;

import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.utils.JDUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        request.setExtras(pageTypeOf(PageType.TOP_CATEGORY));
        return request;
    }


    public static Request subCategoriesRequest(long cid) {
        String url = "http://so.m.jd.com/category/list.action?_format_=json&catelogyId=" + cid;
        Request request = new Request(url);
        request.setExtras(pageTypeOf(PageType.SUB_CATEGORY));
        return request;
    }

    /**
     *
     * @param category
     * @param page
     * @return null if the category should be skipped
     */
    public static Request listRequest(JDCategoryDTO category, int page) {
        if (!shouldFetchItems(category)) {
            return null;
        }

        List<NameValuePair> form = new ArrayList<>();

        form.add(new BasicNameValuePair("_format_", "json"));
        form.add(new BasicNameValuePair("stock", "1"));

        String path = category.getPath();
        final String[] tokens = path.split("_");

        form.add(new BasicNameValuePair("c1", tokens[0]));
        form.add(new BasicNameValuePair("c2", tokens[1]));
        form.add(new BasicNameValuePair("categoryId", tokens[2]));
        form.add(new BasicNameValuePair("page", String.valueOf(page)));


        final Map<String, Object> extras = pageTypeOf(PageType.LIST);
        extras.put("category", category);
        extras.put("page", page);

        extras.put("nameValuePair", form.toArray(new NameValuePair[form.size()]));

        final Request request = new Request("http://so.m.jd.com/ware/searchList.action");
        request.setMethod("POST");
        request.setExtras(extras);
        return request;
    }

    private static Map<String, Object> pageTypeOf(Integer type) {
        Map<String, Object> map = new HashMap<>();
        map.put(PageType.KEY, type);
        return map;
    }
}
