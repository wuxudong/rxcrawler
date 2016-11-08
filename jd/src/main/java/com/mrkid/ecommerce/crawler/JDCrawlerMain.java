package com.mrkid.ecommerce.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:49 PM
 */
@SpringBootApplication
public class JDCrawlerMain {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver",
                "/Users/xudong/workspace/github/ecommerce-crawler/chromedriver/mac/chromedriver");


        final ConfigurableApplicationContext run = SpringApplication.run(JDCrawlerMain.class);
        final JDCrawler jdCrawler = run.getBean(JDCrawler.class);
//        final List<Category> categories = jdCrawler.getAllCategories().join();

        final Category category = new Category();
        category.setLevel(3);
        category.setPath("670_699_700");
        category.setCid(700);

        category.setName("路由器");
        final CompletableFuture<List<Sku>> sku = jdCrawler.getSku(category);

        sku.thenAccept(l -> System.out.println(l)).join();
        run.close();

    }
}
