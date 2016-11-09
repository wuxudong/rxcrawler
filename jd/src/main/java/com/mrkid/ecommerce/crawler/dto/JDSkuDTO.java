package com.mrkid.ecommerce.crawler.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 7:06 PM
 */
@Data
public class JDSkuDTO {

    private long id;
    private String name;
    private BigDecimal price;

    private long cid;
}
