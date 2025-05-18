package com.lostanimals.telegram;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Tokens {
    public static final String TELEGRAM_BOT_NAME;
    public static final String TELEGRAM_BOT_TOKEN;
    static{
        Properties prop = new Properties();
        String token = "config/tgBot2.config";

        // Используем getResourceAsStream для загрузки файла из classpath
        try (InputStream inputStream = Tokens.class.getClassLoader().getResourceAsStream(token)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Tokens file missing: " + token);
            }
            prop.load(inputStream);
        } catch (IOException ex) {
            System.out.println("Error loading tokens: " + ex.getMessage());
        }
        TELEGRAM_BOT_NAME = prop.getProperty("tgBot2.name");
        TELEGRAM_BOT_TOKEN =prop.getProperty("tgBot2.token");

    }
}
