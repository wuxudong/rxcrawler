package com.mrkid.ecommerce.itjuzi;

import com.mrkid.crawler.Request;
import com.mrkid.ecommerce.itjuzi.PageType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 6:43 PM
 */
public class RequestHelper {
    public static Request tokenRequest() {
        final Request request = new Request("http://cobra.itjuzi.com/oauth/access_token");
        request.setMethod("POST");
        request.setPageType(PageType.TOKEN);

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2");
        map.put("client_secret", "7fed6221f1ecad2721e280319bf1cca6");
        map.put("grant_type", "client_credentials");

        request.setForm(map);
        return request;
    }


    public static Request pageRequest(int page) {
        String url = "http://cobra.itjuzi.com/api/company";
        if (page != 0) {
            url = url + "?page=" + page;
        }
        Request request = new Request(url);
        request.setPageType(PageType.PAGE);
        request.putExtra("page", page);
        return request;
    }

    public static Request companyRequest(long id) {
        String url = "http://cobra.itjuzi.com/api/company" + "/" + id;
        Request request = new Request(url);
        request.setPageType(PageType.COMPANY);
        request.putExtra("id", id);
        return request;
    }
}
