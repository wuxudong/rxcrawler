package com.mrkid.ecommerce.crawler.httpasyncclient;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;

/**
 * User: xudong
 * Date: 03/11/2016
 * Time: 4:35 PM
 */
//@Configuration
public class HttpAsynClientConfiguration {
    @Bean
    public CloseableHttpAsyncClient httpAsyncClient() {

        final int TIMEOUT = 60 * 1000;
        // reactor config
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSoTimeout(TIMEOUT).build();

        HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create()
                .setDefaultIOReactorConfig(reactorConfig)
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, " +
                        "like Gecko) Chrome/54.0.2840.71 Safari/537.36")
                .setMaxConnPerRoute(1000)
                .setMaxConnTotal(1000);

        final CloseableHttpAsyncClient httpAsyncClient = asyncClientBuilder.build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }
}
