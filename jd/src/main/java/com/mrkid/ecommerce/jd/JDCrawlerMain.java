package com.mrkid.ecommerce.jd;

import com.mrkid.crawler.Spider;
import com.mrkid.crawler.scheduler.Scheduler;
import com.mrkid.ecommerce.jd.crawler.RequestHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.script.ScriptException;

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

        Scheduler scheduler = context.getBean(Scheduler.class);

        Spider spider = context.getBean(Spider.class);


        if ("restart".equals(args[0])) {
            scheduler.reset();
            spider.addRequest(RequestHelper.topCategoriesRequest());
        } else {
            scheduler.restore();
        }

        spider.start();

        context.close();

    }
}
