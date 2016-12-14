package com.mrkid.crawler.processor;

import com.mrkid.crawler.Page;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompositePageProcessor implements PageProcessor {

    private List<SubPageProcessor> subPageProcessors = new ArrayList<>();

    @Override
    public Page process(Page page) throws Exception {
        for (SubPageProcessor subPageProcessor : subPageProcessors) {
            if (subPageProcessor.match(page.getRequest())) {
                SubPageProcessor.MatchOther matchOtherProcessorProcessor = subPageProcessor.processPage(page);
                if (matchOtherProcessorProcessor == null || matchOtherProcessorProcessor != SubPageProcessor.MatchOther.YES) {
                    return page;
                }
            }
        }

        return page;
    }
}
