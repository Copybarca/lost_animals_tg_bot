package com.lostanimals.animalsInfrastructure;

import com.lostanimals.telegram.TinderBoltApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.lostanimals.animalsInfrastructure.repository")
@EntityScan(basePackages = "com.lostanimals.animalsInfrastructure.model")
public class LostAnimalsApplication {

    public static void main(String[] args)throws TelegramApiException {
        ApplicationContext applicationContext = SpringApplication.run(LostAnimalsApplication.class, args);

    }
}
