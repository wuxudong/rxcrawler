package com.mrkid.ecommerce.crawler.webmagic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.JDCrawlerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.handler.SubPageProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class SubCategoryPageProcessor implements SubPageProcessor {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public MatchOther processPage(Page page) {

        try {
            final JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(page.getRawText()).get
                    ("catalogBranch")
                    .asText());
            final JDCategoryDTO[] categories = objectMapper.treeToValue
                    (jsonNode.get("data"), JDCategoryDTO[].class);


            List<JDCategoryDTO> flatten = new ArrayList<>();


            for (JDCategoryDTO category : categories) {
                flatten.add(category);

                if (category.getCatelogyList() != null) {
                    for (JDCategoryDTO subCategory : category.getCatelogyList()) {
                        flatten.add(subCategory);
                        if (StringUtils.isNotBlank(subCategory.getPath()) && !subCategory.isVirtual()) {

                            final Request request = RequestHelper.listRequest(subCategory, 1);
                            if (request != null) {
                                page.addTargetRequest(request);
                            }
                        }
                    }
                }
            }

            page.putField("categories", flatten);


        } catch (IOException e) {
            throw new JDCrawlerException(e);

        }
        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        return PageType.SUB_CATEGORY.equals(page.getExtra(PageType.KEY));
    }
}
