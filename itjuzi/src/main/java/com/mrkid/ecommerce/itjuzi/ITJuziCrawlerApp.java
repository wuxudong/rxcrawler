package com.mrkid.ecommerce.itjuzi;

import com.mrkid.crawler.Spider;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.script.ScriptException;
import java.io.IOException;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:49 PM
 */
@SpringBootApplication
public class ITJuziCrawlerApp {
    public static void main(String[] args) throws ScriptException, ParseException, IOException, InterruptedException {
        final ConfigurableApplicationContext context = SpringApplication.run(ITJuziCrawlerApp.class);

        Options options = new Options();

        options.addOption("a", "action", true,
                "[crawl|parse|all] -> only crawl? only parse downloaded pages? or do both?");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        HelpFormatter formatter = new HelpFormatter();


        if (!cmd.hasOption("action")) {
            formatter.printHelp("java -jar itjuzi.jar", options);
            return;

        }
        Spider spider = context.getBean(Spider.class);

        final String action = cmd.getOptionValue("action");

        switch (action) {
            case "crawl":
                new ITJuziCrawler().crawl(spider);
                break;

            case "parse":
                new CSVExtractor().parse();
                break;

            case "all":
                new ITJuziCrawler().crawl(spider);
                new CSVExtractor().parse();
                break;

            default:
                formatter.printHelp("java -jar itjuzi.jar", options);
        }

        context.close();

    }
}
