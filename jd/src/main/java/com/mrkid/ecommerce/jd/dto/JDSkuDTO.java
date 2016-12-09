package com.mrkid.ecommerce.jd.dto;

import lombok.Data;

import javax.persistence.Column;
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

    private String rawListContent;

    private String rawItemContent;

    private long cid;
}
