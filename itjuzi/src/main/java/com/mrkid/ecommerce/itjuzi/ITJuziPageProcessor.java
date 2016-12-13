package com.mrkid.ecommerce.itjuzi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class ITJuziPageProcessor implements SubPageProcessor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MatchOther processPage(Page page) throws Exception {
        JsonNode body = objectMapper.readTree(page.getRawText());
        if (Globals.pageCount < 0) {
            int pageCount = body.get("last_page").asInt();

            if (pageCount <= 0) {
                throw new IllegalArgumentException("unexpected pageCount " + pageCount);
            }

            IntStream.range(0, pageCount + 1).boxed()
                    .map(i -> RequestHelper.pageRequest(i))
                    .forEach(r -> page.addTargetRequest(r));

        }


        for (JsonNode node : body.get("data")) {
            final long comId = node.get("com_id").asLong();
            if(!CompanyDumper.isCompanyDone(comId)){
                page.addTargetRequest(RequestHelper.companyRequest(comId));
            }
        }

        System.out.println("page " + page.getRequest().getExtra("page") + " done");

        return MatchOther.NO;

    }

    @Override
    public boolean match(Request page) {
        return page.getPageType().equals(PageType.PAGE);
    }
}
