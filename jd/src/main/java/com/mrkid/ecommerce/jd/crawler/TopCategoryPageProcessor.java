package com.mrkid.ecommerce.jd.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import com.mrkid.ecommerce.jd.dto.JDCategoryDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import java.util.ArrayList;
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
    public MatchOther processPage(Page page) throws Exception {

        final Document doc = Jsoup.parse(page.getRawText());
        final Elements elements = doc.select("script");


        for (Element element : elements) {

            String script = element.data();

            final String key = "jsArgs['category']";
            int start = script.indexOf(key);
            if (start >= 0) {
                int end = script.indexOf(';', start);

                String jsAssignment = script.substring(start, end + 1);

                final Object o = scriptEngine.eval("var jsArgs = {};" + jsAssignment + "jsArgs;");
                final JsonNode list = objectMapper.readTree(objectMapper.writeValueAsString
                        (o)).get("category").get("roorList").get
                        ("catelogyList");

                final Iterator<JsonNode> iterator = list.elements();
                List<JDCategoryDTO> categories = new ArrayList<>();
                while (iterator.hasNext()) {
                    categories.add(objectMapper.treeToValue(iterator.next(), JDCategoryDTO.class));
                }
                page.getResultItems().put("categories", categories);

                for (JDCategoryDTO category : categories) {
                    page.addTargetRequest(RequestHelper.subCategoriesRequest(category.getCid()));
                }

                break;

            }
        }

        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        return PageType.TOP_CATEGORY.equals(page.getPageType());
    }
}
