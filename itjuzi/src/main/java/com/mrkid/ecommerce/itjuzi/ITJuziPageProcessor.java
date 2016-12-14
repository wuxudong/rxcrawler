package com.mrkid.ecommerce.itjuzi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class ITJuziPageProcessor implements SubPageProcessor {
    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(ITJuziPageProcessor.class);


    @Override
    public MatchOther processPage(Page page) throws Exception {
        JsonNode body = objectMapper.readTree(page.getRawText());
        if (Globals.pageCount < 0) {
            int pageCount = body.get("last_page").asInt();

            logger.info("total page count " + pageCount);

            if (pageCount <= 0) {
                throw new IllegalArgumentException("unexpected pageCount " + pageCount);
            }

            Globals.pageCount = pageCount;
        }

        int currentPage = (Integer) page.getRequest().getExtra("page");

        int companyCount = 0;
        for (JsonNode node : body.get("data")) {
            final long comId = node.get("com_id").asLong();
            companyCount++;
            if (!CompanyDumper.isCompanyDone(comId)) {
                page.addTargetRequest(RequestHelper.companyRequest(comId));
            }
        }

        logger.info("page " + currentPage + " has " + companyCount + " companies");


        if (currentPage < Globals.pageCount) {
            page.addTargetRequest(RequestHelper.pageRequest(currentPage + 1));
        }

        logger.info("page " + currentPage + " done");

        return MatchOther.NO;

    }

    @Override
    public boolean match(Request page) {
        return page.getPageType().equals(PageType.PAGE);
    }
}
