package com.mrkid.ecommerce.crawler.model;

import lombok.Data;

import javax.persistence.*;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 7:06 PM
 */
@Data
@Entity
@Table(indexes = @Index(name = "cid_index", columnList = "cid"))
public class JDSku {
    @Id
    private long id;

    @Column(columnDefinition = "TEXT")
    private String name;

    private long cid;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawListContent;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawItemContent;

}
