package com.mrkid.ecommerce.crawler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 5:15 PM
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private int cid;
    private int level;
    private String name;
    private String path;

    private Category[] catelogyList = new Category[0];
}
