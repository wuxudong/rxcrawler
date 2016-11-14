package com.mrkid.ecommerce.crawler.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 8:20 PM
 */
@Data
@Entity
@Table(indexes = {@Index(name = "sku_id_last_check_time_index", columnList = "skuId,lastCheckTime")})
public class JDSkuPriceHistory {
    @Id
    @GeneratedValue
    private long id;

    private long skuId;

    private BigDecimal price;

    private Date lastCheckTime;

    private Date firstCheckTime;
}
