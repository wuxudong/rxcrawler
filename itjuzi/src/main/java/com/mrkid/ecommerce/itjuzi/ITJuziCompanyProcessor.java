package com.mrkid.ecommerce.itjuzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import com.mrkid.crawler.processor.SubPageProcessor;
import org.springframework.stereotype.Component;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:29 PM
 */

@Component
public class ITJuziCompanyProcessor implements SubPageProcessor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MatchOther processPage(Page page) throws Exception {

        long id = (Long)page.getRequest().getExtra("id");

        CompanyDumper.dumpCompany(id, page.getRawText());
        System.out.println("company " + page.getRequest().getExtra("id") + " done");


        return MatchOther.NO;

    }

    @Override
    public boolean match(Request page) {
        return page.getPageType().equals(PageType.COMPANY);
    }
}
