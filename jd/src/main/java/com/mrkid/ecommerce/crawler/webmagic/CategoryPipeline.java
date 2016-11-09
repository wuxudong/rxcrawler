package com.mrkid.ecommerce.crawler.webmagic;

import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.model.JDCategory;
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
public class CategoryPipeline implements SubPipeline {

    @Autowired
    private JDService jdService;

    @Override
    public MatchOther processResult(ResultItems resultItems, Task task) {

        List<JDCategoryDTO> categories = resultItems.get("categories");
        for (JDCategoryDTO categoryDTO : categories) {

            jdService.saveCategory(categoryDTO);
        }

        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        final Object pageType = page.getExtra(PageType.KEY);
        return PageType.TOP_CATEGORY.equals(pageType) || PageType.SUB_CATEGORY.equals(pageType);
    }
}
