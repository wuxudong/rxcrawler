package com.mrkid.ecommerce.jd;

import com.mrkid.crawler.Site;
import com.mrkid.crawler.Spider;
import com.mrkid.crawler.downloader.Downloader;
import com.mrkid.crawler.downloader.HttpAsyncClientDownloader;
import com.mrkid.crawler.pipeline.CompositePipeline;
import com.mrkid.crawler.pipeline.Pipeline;
import com.mrkid.crawler.pipeline.SubPipeline;
import com.mrkid.crawler.processor.CompositePageProcessor;
import com.mrkid.crawler.processor.PageProcessor;
import com.mrkid.crawler.processor.SubPageProcessor;
import com.mrkid.crawler.scheduler.RedisScheduler;
import com.mrkid.crawler.scheduler.Scheduler;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

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
    public Site site() {
        Site site = new Site();
        site.setSleepTime(0);
        site.setRetrySleepTime(1000);
        site.setRetryTimes(5);
        site.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/54.0.2840.71 Safari/537.36");

        site.setTimeOut(30 * 1000);

        // use squid as proxy, you may add parent of proxies in squid
        site.setHttpProxy(new HttpHost("127.0.0.1", 3128));

        return site;
    }

    @Bean
    public PageProcessor jdPageProcessor() {
        CompositePageProcessor pageProcessor = new CompositePageProcessor();
        pageProcessor.setSubPageProcessors(subPageProcessors);

        return pageProcessor;
    }

    @Bean
    public Pipeline jdPipeline() {
        CompositePipeline pipeline = new CompositePipeline();
        pipeline.setSubPipelines(subPipelines);

        return pipeline;
    }

    @Bean
    public Scheduler scheduler(StringRedisTemplate redisTemplate) {
        RedisScheduler redisScheduler = new RedisScheduler();
        redisScheduler.setRedisTemplate(redisTemplate);
        return redisScheduler;
    }

    @Bean
    public CloseableHttpAsyncClient httpAsyncClient(Site site) {
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(site.getTimeOut())
                .setSoTimeout(site.getTimeOut()).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();
        asyncClientBuilder.setDefaultIOReactorConfig(reactorConfig);

        asyncClientBuilder.setUserAgent(site.getUserAgent());

        if (site.getHttpProxy() != null) {
            asyncClientBuilder.setProxy(site.getHttpProxy());
        }

        asyncClientBuilder.setMaxConnPerRoute(1000).setMaxConnTotal(1000);

        final CloseableHttpAsyncClient asyncClient = asyncClientBuilder.build();
        asyncClient.start();

        return asyncClient;
    }

    @Bean
    public Downloader downloader(CloseableHttpAsyncClient httpAsyncClient) {
        return new HttpAsyncClientDownloader(httpAsyncClient);
    }

    @Bean
    public Spider spider(Site site, Downloader downloader, Scheduler scheduler,
                         PageProcessor pageProcessor, Pipeline pipeline) {
        Spider spider = new Spider();
        spider.setDownloader(downloader);
        spider.setPageProcessor(pageProcessor);
        spider.setSite(site);
        spider.setScheduler(scheduler);
        spider.setPipeline(pipeline);
        spider.setMaxConcurrency(200);
        return spider;
    }
}
