package com.mrkid.ecommerce.crawler.webmagic;

import com.mrkid.ecommerce.crawler.dto.JDSkuDTO;
import com.mrkid.ecommerce.crawler.model.JDSku;
import com.mrkid.ecommerce.crawler.service.JDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.handler.SubPipeline;

import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 7:09 PM
 */
@Component
public class SkuPipeline implements SubPipeline {

    @Autowired
    private JDService jdService;

    @Override
    public MatchOther processResult(ResultItems resultItems, Task task) {

        List<JDSkuDTO> categories = resultItems.get("skus");
        for (JDSkuDTO skuDTO : categories) {
            jdService.saveSku(skuDTO);
        }

        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        final Object pageType = page.getExtra(PageType.KEY);
        return PageType.LIST.equals(pageType);
    }
}
