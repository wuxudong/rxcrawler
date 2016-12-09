package com.mrkid.ecommerce.jd.service;

import com.mrkid.ecommerce.jd.model.JDCategory;
import com.mrkid.ecommerce.jd.model.JDSku;
import com.mrkid.ecommerce.jd.model.JDSkuPriceHistory;
import com.mrkid.ecommerce.jd.repository.JDCategoryRepository;
import com.mrkid.ecommerce.jd.repository.JDSkuHistoryRepository;
import com.mrkid.ecommerce.jd.repository.JDSkuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 7:12 PM
 */
@Component
public class JDService {
    @Autowired
    private JDCategoryRepository categoryRepository;


    @Autowired
    private JDSkuRepository skuRepository;


    @Autowired
    private JDSkuHistoryRepository skuHistoryRepository;

    @Transactional
    public JDCategory saveCategory(JDCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public JDSkuPriceHistory saveSku(JDSku sku, BigDecimal price) {
        skuRepository.save(sku);

        final Date current = new Date();

        final long id = sku.getId();

        JDSkuPriceHistory skuPriceHistory = skuHistoryRepository.findFirstBySkuIdOrderByLastCheckTimeDesc(id);

        if (skuPriceHistory != null && skuPriceHistory.getPrice().setScale(2).equals(price)) {
            skuPriceHistory.setLastCheckTime(current);
        } else {
            skuPriceHistory = new JDSkuPriceHistory();
            skuPriceHistory.setSkuId(id);
            skuPriceHistory.setPrice(price);

            skuPriceHistory.setFirstCheckTime(current);
            skuPriceHistory.setLastCheckTime(current);
        }
        return skuHistoryRepository.save(skuPriceHistory);
    }

}
