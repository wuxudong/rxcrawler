package com.mrkid.ecommerce.crawler;

import com.mrkid.ecommerce.crawler.webmagic.ExtrasAwareFileCacheQueueScheduler;
import com.mrkid.ecommerce.crawler.webmagic.RequestHelper;
import com.mrkid.ecommerce.crawler.webmagic.StrictHashSetDuplicateRemover;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.script.ScriptException;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:49 PM
 */
@SpringBootApplication
public class JDCrawlerMain {
    public static void main(String[] args) throws ScriptException {
        final ConfigurableApplicationContext context = SpringApplication.run(JDCrawlerMain.class);

        Spider.create(context.getBean(PageProcessor.class))
                .scheduler(new ExtrasAwareFileCacheQueueScheduler("./WebMagicFileCacheQueue")
                        .setDuplicateRemover(new StrictHashSetDuplicateRemover()))
                .addPipeline(new ConsolePipeline())
                .addPipeline(context.getBean(Pipeline.class))
                .addRequest(RequestHelper.topCategoriesRequest()).thread(5).run();

        context.close();

    }
}
