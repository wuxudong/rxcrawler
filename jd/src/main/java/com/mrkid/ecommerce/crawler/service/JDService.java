package com.mrkid.ecommerce.crawler.service;

import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.dto.JDSkuDTO;
import com.mrkid.ecommerce.crawler.model.JDCategory;
import com.mrkid.ecommerce.crawler.model.JDSku;
import com.mrkid.ecommerce.crawler.model.JDSkuPriceHistory;
import com.mrkid.ecommerce.crawler.repository.JDCategoryRepository;
import com.mrkid.ecommerce.crawler.repository.JDSkuHistoryRepository;
import com.mrkid.ecommerce.crawler.repository.JDSkuRepository;
import org.apache.commons.lang3.StringUtils;
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
    public JDCategory saveCategory(JDCategoryDTO categoryDTO) {
        assert categoryDTO.getCid() != 0;
        assert StringUtils.isBlank(categoryDTO.getName());

        JDCategory category = new JDCategory();
        category.setCid(categoryDTO.getCid());
        category.setName(categoryDTO.getName());
        category.setPath(categoryDTO.getPath());
        category.setLevel(categoryDTO.getLevel());

        category.setVirtual(categoryDTO.isVirtual());

        return categoryRepository.save(category);
    }


    @Transactional
    public JDSkuPriceHistory saveSku(JDSkuDTO skuDTO) {
        assert skuDTO.getId() != 0;
        assert skuDTO.getCid() != 0;
        assert skuDTO.getPrice() != null;
        assert StringUtils.isBlank(skuDTO.getName());


        JDSku sku = new JDSku();
        sku.setId(skuDTO.getId());
        sku.setName(skuDTO.getName());

        sku.setCid(skuDTO.getCid());

        skuRepository.save(sku);


        final Date current = new Date();

        JDSkuPriceHistory skuPriceHistory = skuHistoryRepository.findFirstBySkuIdOrderByLastCheckTimeDesc(skuDTO.getId());

        BigDecimal price = skuDTO.getPrice().setScale(2);
        if (skuPriceHistory != null && skuPriceHistory.getPrice().setScale(2).equals(price)) {
            skuPriceHistory.setLastCheckTime(current);
        } else {
            skuPriceHistory = new JDSkuPriceHistory();
            skuPriceHistory.setSkuId(skuDTO.getId());
            skuPriceHistory.setPrice(price);

            skuPriceHistory.setFirstCheckTime(current);
            skuPriceHistory.setLastCheckTime(current);
        }
        return skuHistoryRepository.save(skuPriceHistory);
    }

}
