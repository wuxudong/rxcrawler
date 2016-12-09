package com.mrkid.crawler.pipeline;


import com.mrkid.crawler.RequestMatcher;
import com.mrkid.crawler.ResultItems;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompositePipeline implements Pipeline {

    private List<SubPipeline> subPipelines = new ArrayList<>();

    @Override
    public void process(ResultItems resultItems) throws Exception {
        for (SubPipeline subPipeline : subPipelines) {
            if (subPipeline.match(resultItems.getRequest())) {
                RequestMatcher.MatchOther matchOtherProcessorProcessor = subPipeline.processResult(resultItems);
                if (matchOtherProcessorProcessor == null || matchOtherProcessorProcessor != RequestMatcher.MatchOther.YES) {
                    return;
                }
            }
        }
    }
}
