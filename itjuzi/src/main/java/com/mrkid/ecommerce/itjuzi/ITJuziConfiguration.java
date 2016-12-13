package com.mrkid.ecommerce.itjuzi;

import com.mrkid.crawler.Site;
import com.mrkid.crawler.Spider;
import com.mrkid.crawler.downloader.Downloader;
import com.mrkid.crawler.downloader.HttpAsyncClientDownloader;
import com.mrkid.crawler.processor.CompositePageProcessor;
import com.mrkid.crawler.processor.PageProcessor;
import com.mrkid.crawler.scheduler.MemoryScheduler;
import com.mrkid.crawler.scheduler.Scheduler;
import org.apache.http.*;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: xudong
 * Date: 08/11/2016
 * Time: 5:00 PM
 */


@Configuration
public class ITJuziConfiguration {
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
//        site.setHttpProxy(new HttpHost("127.0.0.1", 3128));

        return site;
    }

    @Bean
    public PageProcessor itjuziPageProcessor() {
        CompositePageProcessor pageProcessor = new CompositePageProcessor();
        pageProcessor.setSubPageProcessors(Arrays.asList(new ITJuziTokenProcessor(), new ITJuziPageProcessor(), new
                ITJuziCompanyProcessor()));

        return pageProcessor;
    }

    @Bean
    public Scheduler scheduler() {
        return new MemoryScheduler();
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

        asyncClientBuilder.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext httpContext) throws HttpException, IOException {
                if (Globals.token != null) {
                    request.addHeader("Authorization", "Bearer " + Globals.token);
                }
            }
        });

        asyncClientBuilder.setMaxConnPerRoute(100).setMaxConnTotal(100);

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
                         PageProcessor pageProcessor) {
        Spider spider = new Spider();
        spider.setDownloader(downloader);
        spider.setPageProcessor(pageProcessor);
        spider.setPipeline(resultItems -> {
        });
        spider.setSite(site);
        spider.setScheduler(scheduler);
        spider.setMaxConcurrency(200);
        return spider;
    }
}
