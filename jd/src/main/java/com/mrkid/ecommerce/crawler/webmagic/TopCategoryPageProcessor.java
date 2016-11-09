package com.mrkid.ecommerce.crawler.webmagic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.model.Category_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.handler.SubPageProcessor;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class TopCategoryPageProcessor implements SubPageProcessor {

    @Autowired
    private ScriptEngine scriptEngine;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public MatchOther processPage(Page page) {

        for (String script : page.getHtml().$("script").all()) {

            final String key = "jsArgs['category']";
            int start = script.indexOf(key);
            if (start >= 0) {
                int end = script.indexOf(';', start);

                String jsAssignment = script.substring(start, end + 1);

                try {
                    final Object o = scriptEngine.eval("var jsArgs = {};" + jsAssignment + "jsArgs;");
                    final JsonNode list = objectMapper.readTree(objectMapper.writeValueAsString
                            (o)).get("category").get("roorList").get
                            ("catelogyList");

                    final Iterator<JsonNode> iterator = list.elements();
                    List<JDCategoryDTO> categories = new ArrayList<>();
                    while (iterator.hasNext()) {
                        categories.add(objectMapper.treeToValue(iterator.next(), JDCategoryDTO.class));
                    }
                    page.putField("categories", categories);


                    for (JDCategoryDTO category : categories) {
                        page.addTargetRequest(RequestHelper.subCategoriesRequest(category.getCid()));
                    }

                    break;

                } catch (Exception e) {
                    logger.error("fail to extract categories, " + page.getRawText(), e);
                    page.putField("categories", new ArrayList<>());
                }
            }
        }

        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        return PageType.TOP_CATEGORY.equals(page.getExtra(PageType.KEY));
    }
}
