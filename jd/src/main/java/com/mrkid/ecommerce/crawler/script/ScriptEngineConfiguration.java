package com.mrkid.ecommerce.crawler.script;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * User: xudong
 * Date: 09/11/2016
 * Time: 1:50 PM
 */
@Configuration
public class ScriptEngineConfiguration {
    @Bean
    public ScriptEngine scriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        return factory.getEngineByName("JavaScript");
    }


}
