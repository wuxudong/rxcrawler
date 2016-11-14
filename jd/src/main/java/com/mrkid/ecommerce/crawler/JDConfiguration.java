package com.mrkid.ecommerce.crawler;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.handler.CompositePipeline;
import us.codecraft.webmagic.handler.SubPageProcessor;
import us.codecraft.webmagic.handler.SubPipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:00 PM
 */


@Configuration
public class JDConfiguration {
    @Autowired
    private List<SubPageProcessor> subPageProcessors;

    @Autowired
    private List<SubPipeline> subPipelines;

    @Bean
    public PageProcessor initJDPageProcessor() {
        Site site = Site.me().setSleepTime(0).setRetryTimes(3).setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X " +
                "10_12_1)" +
                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36");

        site.setTimeOut(30000);
        site.setHttpProxy(new HttpHost("127.0.0.1", 3128));

        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.setSubPageProcessors(subPageProcessors.toArray(new SubPageProcessor[subPageProcessors.size()]));

        return pageProcessor;
    }

    @Bean
    public Pipeline initJDPipeline() {

        CompositePipeline pipeline = new CompositePipeline();
        pipeline.setSubPipeline(subPipelines.toArray(new SubPipeline[subPipelines.size()]));

        return pipeline;
    }
}
