package com.mrkid.ecommerce.jd.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import com.mrkid.ecommerce.jd.dto.JDCategoryDTO;
import com.mrkid.ecommerce.jd.dto.JDSkuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class ListPageProcessor implements SubPageProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());


    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MatchOther processPage(Page page) throws Exception {

        final JsonNode value = objectMapper.readTree(objectMapper.readTree(page.getRawText()).get("value").asText());

        List<JDSkuDTO> result = new ArrayList<>();
        for (JsonNode node : value.get("wareList")) {
            JDSkuDTO sku = new JDSkuDTO();
            sku.setId(node.get("wareId").asLong());
            sku.setName(node.get("wname").asText());
            sku.setPrice(BigDecimal.valueOf(node.get("jdPrice").asDouble()));
            sku.setCid(node.get("catid").asLong());

            sku.setRawListContent(node.toString());

            if (sku.getId() != 0) {
                result.add(sku);
            } else {
                logger.error("extract sku whose id is 0, " + value);
            }
        }

        final Object obj = page.getRequest().getExtra("category");


        JDCategoryDTO category = null;
        if (obj instanceof JDCategoryDTO) {
            category = (JDCategoryDTO) obj;
        } else {
            category = objectMapper.readValue(objectMapper.writeValueAsString(obj),
                    JDCategoryDTO.class);
        }

        int pageNum = Integer.valueOf(page.getRequest().getForm().get("page"));

        if (!result.isEmpty()) {
            page.addTargetRequest(RequestHelper.listRequest(category, pageNum + 1));
        }

        page.getResultItems().put("skus", result);

        logger.info("category:{} page:{} get {} skus", category.getPath(), pageNum, result.size());


        return MatchOther.NO;
    }

    @Override
    public boolean match(Request page) {
        return PageType.LIST.equals(page.getPageType());
    }
}
