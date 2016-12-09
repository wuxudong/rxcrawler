package com.mrkid.ecommerce.jd.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import com.mrkid.ecommerce.jd.dto.JDCategoryDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    public MatchOther processPage(Page page) throws Exception {

        final JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(page.getRawText()).get
                ("catalogBranch")
                .asText());
        final JDCategoryDTO[] categories = objectMapper.treeToValue
                (jsonNode.get("data"), JDCategoryDTO[].class);


        List<JDCategoryDTO> list = new ArrayList<>();


        for (JDCategoryDTO category : categories) {
            list.add(category);

            if (category.getCatelogyList() != null) {
                for (JDCategoryDTO subCategory : category.getCatelogyList()) {
                    list.add(subCategory);
                    if (StringUtils.isNotBlank(subCategory.getPath()) && !subCategory.isVirtual()) {

                        final Request request = RequestHelper.listRequest(subCategory, 1);
                        if (request != null) {
                            page.addTargetRequest(request);
                        }
                    }
                }
            }
        }

        page.getResultItems().put("categories", list);


        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        return PageType.SUB_CATEGORY.equals(page.getPageType());
    }
}
