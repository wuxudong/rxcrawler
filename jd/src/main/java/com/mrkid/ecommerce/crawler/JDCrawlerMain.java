package com.mrkid.ecommerce.crawler;

import com.mrkid.ecommerce.crawler.webmagic.DummyRedisScheduler;
import com.mrkid.ecommerce.crawler.webmagic.RequestHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.script.ScriptException;
import java.util.Arrays;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:49 PM
 */
@SpringBootApplication
public class JDCrawlerMain {
    public static void main(String[] args) throws ScriptException {
        if (args.length != 1) {
            System.out.println("usage : java -jar jd.jar restart/resume");
            return;
        }

        final ConfigurableApplicationContext context = SpringApplication.run(JDCrawlerMain.class);

        final DummyRedisScheduler scheduler = new DummyRedisScheduler(context.getBean(StringRedisTemplate.class));

        final Spider spider = Spider.create(context.getBean(PageProcessor.class))
                .scheduler(scheduler)
                .setSpiderListeners(Arrays.asList(scheduler))
                .addPipeline(context.getBean(Pipeline.class));


        switch (args[0]) {
            case "restart":
                scheduler.clearAll();
                spider.addRequest(RequestHelper.topCategoriesRequest());
                break;
            case "resume":
                break;
        }


        spider.thread(100).run();

        context.close();

    }
}
