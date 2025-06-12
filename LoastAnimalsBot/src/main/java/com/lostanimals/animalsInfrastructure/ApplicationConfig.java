package com.lostanimals.animalsInfrastructure;

import com.lostanimals.telegram.TinderBoltApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.springframework.context.ApplicationContext;
@Configuration
public class ApplicationConfig{
    private  final ApplicationContext applicationContext;

    @Autowired
    public ApplicationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    @Bean("tinderBoltApp")
    public TinderBoltApp tinderBoltApp(ApplicationContext applicationContext){
        return new TinderBoltApp(applicationContext);
    }
    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(applicationContext.getBean("tinderBoltApp", TinderBoltApp.class));
        return telegramBotsApi;
    }



}
