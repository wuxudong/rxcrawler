package com.mrkid.ecommerce.crawler;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 7:02 PM
 */
@Configuration

public class ChromeDriverConfiguration {

    @Bean
    public ChromeDriver initChromeDriver() {
        return new ChromeDriver(DesiredCapabilities.chrome());
    }

}
