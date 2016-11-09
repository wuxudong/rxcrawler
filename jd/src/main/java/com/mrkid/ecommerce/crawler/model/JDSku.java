package com.mrkid.ecommerce.crawler.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 7:06 PM
 */
@Data
@Entity
public class JDSku {
    @Id
    private long id;

    @Column(columnDefinition = "TEXT")
    private String name;

    private long cid;
}
