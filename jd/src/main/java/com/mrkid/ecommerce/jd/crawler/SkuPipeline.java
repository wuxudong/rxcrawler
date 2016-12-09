package com.mrkid.ecommerce.jd.crawler;

import com.mrkid.crawler.Request;
import com.mrkid.crawler.ResultItems;
import com.mrkid.crawler.pipeline.SubPipeline;
import com.mrkid.ecommerce.jd.dto.JDSkuDTO;
import com.mrkid.ecommerce.jd.facade.JDFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 7:09 PM
 */
@Component
public class SkuPipeline implements SubPipeline {

    @Autowired
    private JDFacade jdFacade;

    @Override
    public MatchOther processResult(ResultItems resultItems) {

        List<JDSkuDTO> skus = resultItems.get("skus");
        for (JDSkuDTO skuDTO : skus) {
            jdFacade.saveSku(skuDTO);
        }

        return MatchOther.NO;
    }

    @Override
    public boolean match(Request request) {
        return PageType.LIST.equals(request.getPageType());
    }
}
