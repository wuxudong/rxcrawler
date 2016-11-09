package com.mrkid.ecommerce.crawler.repository;

import com.mrkid.ecommerce.crawler.model.JDSku;
import com.mrkid.ecommerce.crawler.model.JDSkuPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: xudong
 * Date: 21/10/2016
 * Time: 11:43 AM
 */
public interface JDSkuHistoryRepository extends JpaRepository<JDSkuPriceHistory, Long> {
    JDSkuPriceHistory findOneBySkuIdOrderByLastCheckTimeDesc(long skuId);

}
