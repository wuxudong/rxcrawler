package com.mrkid.ecommerce.crawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 5:15 PM
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JDCategoryDTO {
    private long cid;
    private int level;
    private String name;
    private String path;

    private JDCategoryDTO[] catelogyList = new JDCategoryDTO[0];

    public boolean isVirtual() {
        return cid >= 100000000 || cid < 0;
    }
}
