package com.mrkid.ecommerce.crawler;

import com.mrkid.ecommerce.crawler.webmagic.AsyncDownloader;
import com.mrkid.ecommerce.crawler.webmagic.AsyncSpider;
import com.mrkid.ecommerce.crawler.webmagic.DummyRedisScheduler;
import com.mrkid.ecommerce.crawler.webmagic.RequestHelper;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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

        final DummyRedisScheduler scheduler = context.getBean(DummyRedisScheduler.class);


        AsyncDownloader asyncDownloader = new AsyncDownloader(context.getBean(CloseableHttpAsyncClient.class));


        final AsyncSpider asyncSpider = new AsyncSpider(context.getBean(PageProcessor.class));
        asyncSpider
                .setAsyncDownloader(asyncDownloader)
                .scheduler(scheduler)
                .setSpiderListeners(Arrays.asList(scheduler))
                .addPipeline(context.getBean(Pipeline.class));


        if ("restart".equals(args[0])) {
            scheduler.clearAll();
            asyncSpider.addRequest(RequestHelper.topCategoriesRequest());
        } else {
            scheduler.mergeProcessingToPending();
        }

        asyncSpider.thread(8).run();



        context.close();

    }
}
