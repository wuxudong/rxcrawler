package com.mrkid.ecommerce.jd.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 5:15 PM
 */
@Data
@Entity
public class JDCategory {
    @Id
    private long cid;
    private int level;
    @Column(columnDefinition = "TEXT")
    private String name;
    private String path;
    private boolean virtual;
}
