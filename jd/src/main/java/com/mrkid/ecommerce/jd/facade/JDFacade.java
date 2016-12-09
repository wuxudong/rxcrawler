package com.mrkid.ecommerce.jd.facade;

import com.mrkid.ecommerce.jd.dto.JDCategoryDTO;
import com.mrkid.ecommerce.jd.dto.JDSkuDTO;
import com.mrkid.ecommerce.jd.model.JDCategory;
import com.mrkid.ecommerce.jd.model.JDSku;
import com.mrkid.ecommerce.jd.service.JDService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * User: xudong
 * Date: 07/12/2016
 * Time: 10:28 PM
 */
@Component

public class JDFacade {
    @Autowired
    private JDService jdService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Logger logger = LoggerFactory.getLogger(JDFacade.class);

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

        return jdService.saveCategory(category);
    }

    @Transactional
    public void saveSku(JDSkuDTO skuDTO) {
        assert skuDTO.getId() != 0;
        assert skuDTO.getCid() != 0;
        assert skuDTO.getPrice() != null;
        assert StringUtils.isBlank(skuDTO.getName());

        final long id = skuDTO.getId();
        final BigDecimal price = skuDTO.getPrice().setScale(2);
        final BigDecimal cachedPrice = getCachedPrice(id);

        if (!price.equals(cachedPrice)) {
            JDSku sku = new JDSku();
            sku.setId(skuDTO.getId());
            sku.setName(skuDTO.getName());
            sku.setRawListContent(skuDTO.getRawListContent());
            sku.setRawItemContent(skuDTO.getRawItemContent());

            sku.setCid(skuDTO.getCid());

            jdService.saveSku(sku, price);

            cachePrice(id, skuDTO.getPrice());
        }
    }

    private BigDecimal getCachedPrice(long skuId) {
        final String value = redisTemplate
                .<String, String>boundHashOps(String.valueOf(skuId / 500)).get(String.valueOf(skuId));

        if (value == null) {
            return null;
        } else {
            return new BigDecimal(value).setScale(2);
        }
    }

    private void cachePrice(long skuId, BigDecimal price) {
        redisTemplate
                .<String, String>boundHashOps(String.valueOf(skuId / 500)).put(String.valueOf(skuId), price.toString());
    }


}