package com.lostanimals.animalsInfrastructure;

import com.lostanimals.telegram.TinderBoltApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class LostAnimalsApplication {

    public static void main(String[] args)throws TelegramApiException {
        ApplicationContext applicationContext = SpringApplication.run(LostAnimalsApplication.class, args);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp(applicationContext));
    }
}
