package com.mrkid.ecommerce.itjuzi;

import com.mrkid.crawler.Spider;
import io.reactivex.Flowable;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:49 PM
 */
public class ITJuziCrawler {
    public void crawl(Spider spider) {
        // init token
        spider.addRequest(RequestHelper.tokenRequest());
        spider.start();

        // refresh token
        Flowable.interval(300, 300, TimeUnit.SECONDS).subscribe(l -> spider.addRequest(RequestHelper.tokenRequest()));

        // crawl
        spider.addRequest(RequestHelper.pageRequest(0));
        spider.start();
    }
}
