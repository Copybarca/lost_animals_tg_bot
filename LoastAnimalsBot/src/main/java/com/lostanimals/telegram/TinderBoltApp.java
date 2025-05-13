package com.lostanimals.telegram;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.AnimalType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {

    private ChatGPTService chatGPT = new ChatGPTService(Tokens.OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;
    private int questionCount;
    private ArrayList<String> messageList = new ArrayList<>();

    private User user;
    private LostAnimals lostAnimal = new LostAnimals(null,null,null,"","",0,null,null,null);



    public TinderBoltApp() {
        super(Tokens.TELEGRAM_BOT_NAME, Tokens.TELEGRAM_BOT_TOKEN);
    }
    @Override
    public void onUpdateEventReceived(Update update) throws Exception {
        //TODO: основной функционал бота будем писать здесь
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message ="";
            var data = update.getMessage();
            if(data!=null)
                message = data.getText()==null?"":data.getText();
            switch(message){
                case "/start":
                    dialogMode = DialogMode.MAIN;
                    sendPhotoMessage("main");
                    sendTextMessage(loadMessage("main"));
                    showMainMenu(
                            "Начало","/start",
                            "Оставить заявку о потере ","/lost",
                            "Оставить заявку о нахождении ","/found",
                            "Посмотреть анкеты найденых ","/found_profiles",
                            "Мои анкеты ","/my_profiles"
                    );
                    return;
                case "/lost"://TODO: доработать ветку алгоритма
                    dialogMode = DialogMode.LOST;
                    sendPhotoMessage("found");
                    questionCount = 0;
                    sendHtmlMessage("Укажи свою контактную информацию.\nВаш TG_ID будет приписан анкете автоматически.");
                    sendTextMessage("Укажите телефон по желанию в формате 8xxxxxxxxxx");

                    //TODO: проверить номер на соответствие форме
                    return;
                case "/found"://TODO: доработать ветку алгоритма
                    dialogMode = DialogMode.FOUND;
                    return;
                case "/found_profiles":
                    dialogMode = DialogMode.MOCK;
                    return;
                case "/my_profiles":
                    dialogMode = DialogMode.MOCK;
                    return;
                default:
                    break;
            }

            switch(dialogMode){
                case LOST://TODO: доработать ветку алгоритма
                    if(questionCount==0){
                        String userNumber = message;
                        String userTgId = String.valueOf(update.getMessage().getFrom().getId());
                        user = new User(userTgId,userNumber);
                        sendAnimalTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }if(questionCount==1){
                        sendTextMessage("Введите кличку животного");
                        int animalsAge = Integer.parseInt(message);
                        lostAnimal.setAge(animalsAge);
                        questionCount = 2;
                        return;
                    }if(questionCount==2){
                        sendTextMessage("Введите пол животного");
                        String animalsName = message;
                        lostAnimal.setName(animalsName);
                        questionCount = 3;
                        return;
                    }if(questionCount==3){
                        sendTextMessage("Введите город пропажи");
                        String animalsSex = message;
                        lostAnimal.setSex(animalsSex);
                        questionCount = 4;
                        return;
                    }if(questionCount==4){
                        sendTextMessage("Введите район пропажи");
                        String animalsCity = message;
                        lostAnimal.setCity(animalsCity);
                        questionCount = 5;
                        return;
                    }if(questionCount==5){
                        sendTextMessage("Введите приметы животного в свободной форме");

                        String animalsDistrict = message;
                        lostAnimal.setDistrict(animalsDistrict);

                        StatusType animalsStatus = StatusType.LOST;
                        lostAnimal.setStatus(animalsStatus);

                        questionCount = 6;
                        return;
                    }if(questionCount==6){
                        sendTextMessage("Введите дату в формате гггг-мм-дд");
                        String animalsDescription = message;
                        lostAnimal.setDescription(animalsDescription);
                        questionCount = 7;
                        return;
                    }if(questionCount==7){
                        String dateStr = message;
                        String[] arr = message.split("-");
                        if(arr.length<3){
                            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
                            return;
                        }
                        int year = Integer.parseInt(arr[0]);
                        int month = Integer.parseInt(arr[1]);
                        int day = Integer.parseInt(arr[2]);
                        java.sql.Date date = new java.sql.Date(year,month,day);
                        lostAnimal.setDate(date);
                        questionCount = 8;
                        sendTextMessage("Прикрепите фото животного");
                        return;
                    }
                    break;
                case FOUND://TODO: доработать ветку алгоритма
                    break;
                default:
                    break;
            }
        }else if (update.hasCallbackQuery()) {
            String animalsType = update.getCallbackQuery().getData();
            boolean isReady = false;
            switch (animalsType) {
                case "Кошка":
                    lostAnimal.setType(AnimalType.CAT);
                    isReady = true;
                    break;
                case "Собака":
                    lostAnimal.setType(AnimalType.DOG);
                    isReady = true;
                    break;
                case "Птица":
                    lostAnimal.setType(AnimalType.PARROT);
                    isReady = true;
                    break;
                default:
                    // Если тип животного не распознан, отправляем сообщение об ошибке
                    sendTextMessage("Введены невалидные данные, выберите пункт кнопки заново");
                    sendAnimalTypeKeyboard(update.getMessage().getChatId());
                    return;
            }
            if (isReady) {
                sendTextMessage("Вы выбрали: " + animalsType);
                sendTextMessage("Сколько лет животному");
                questionCount = 1; // Переход к следующему вопросу
            }
        }else if (update.hasMessage()) {
            if(questionCount==8) {
                sendTextMessage("Это финальный пункт");
                String fileId = update.getMessage().getPhoto().get(0).getFileId();
                File file = execute(new org.telegram.telegrambots.meta.api.methods.GetFile(fileId));
                String filePath = file.getFilePath();
                byte[] photo = downloadNewFile(filePath);
                lostAnimal.setImageData(photo);
                if (user.getLostAnimals() == null) {
                    user.setLostAnimals(new ArrayList<>());
                }
                user.addLostAnimals(lostAnimal);
                questionCount = 9;
                return;
            }
            update.getMessage().getPhoto().get(0);

        }
    }
    private void sendAnimalTypeKeyboard(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Кошка").callbackData("Кошка").build());
        row.add(InlineKeyboardButton.builder().text("Собака").callbackData("Собака").build());
        row.add(InlineKeyboardButton.builder().text("Птица").callbackData("Птица").build());
        buttons.add(row);

        markup.setKeyboard(buttons);
        sendMessageWithKeyboard(chatId, "Выберите вид животного:", markup);
    }
    private void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private byte[] downloadNewFile(String filePath) throws Exception {
        // Формируем URL для скачивания файла
        String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;

        try (InputStream in = new URL(fileUrl).openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray(); // Возвращаем массив байтов
        }
    }
   /* public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }*/
}
