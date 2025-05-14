package com.lostanimals.telegram;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.AnimalType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.service.UserService;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {

    private DialogMode dialogMode = null;
    private int questionCount;
    private final ApplicationContext applicationContext;
    private final UserService userService;
    private final User user;
    private final LostAnimals lostAnimal;

    public TinderBoltApp(ApplicationContext applicationContext) {
        super(Tokens.TELEGRAM_BOT_NAME, Tokens.TELEGRAM_BOT_TOKEN);
        this.applicationContext=applicationContext;
        this.userService = applicationContext.getBean(UserService.class);
        this.user = applicationContext.getBean(User.class);
        this.lostAnimal = applicationContext.getBean(LostAnimals.class);
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
                case LOST:
                    if(questionCount==0){
                        String userTgId = String.valueOf(update.getMessage().getFrom().getId());
                        user.setTgId(userTgId);
                        user.setPhoneNumber(message);
                        sendAnimalTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }if(questionCount==1){
                        sendTextMessage("Введите кличку животного");
                        int animalsAge = Integer.parseInt(message);
                        lostAnimal.setAge(animalsAge);
                        questionCount = 2;
                        return;
                    }if(questionCount==2){//TODO: сделать енам на выбор пола и кнопку добавить - она в калбек квери на else if Будет обрабатываться
                        sendTextMessage("Введите пол животного");
                        lostAnimal.setName(message);
                        questionCount = 3;
                        return;
                    }if(questionCount==3){
                        sendTextMessage("Введите город пропажи");
                        lostAnimal.setSex(message);
                        questionCount = 4;
                        return;
                    }if(questionCount==4){
                        sendTextMessage("Введите район пропажи");
                        lostAnimal.setCity(message);
                        questionCount = 5;
                        return;
                    }if(questionCount==5){
                        sendTextMessage("Введите приметы животного в свободной форме");

                        lostAnimal.setDistrict(message);

                        StatusType animalsStatus = StatusType.LOST;
                        lostAnimal.setStatus(animalsStatus);

                        questionCount = 6;
                        return;
                    }if(questionCount==6){
                        sendTextMessage("Введите дату в формате гггг-мм-дд");
                        lostAnimal.setDescription(message);
                        questionCount = 7;
                        return;
                    }if(questionCount==7){
                        String[] arr = message.split("-");
                        if(arr.length<3){
                            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
                            return;
                        }
                        java.sql.Date date;
                        try{
                            date = Date.valueOf(message);
                            lostAnimal.setDate(date);
                            questionCount = 8;
                            sendTextMessage("Прикрепите фото животного как ФАЙЛ БЕЗ СЖАТИЯ.\n Размер фото до 1МБ включительно.");
                        }catch (IllegalArgumentException e){
                            e.printStackTrace();//TODO: тут можно добавить сервис для логирования потом
                            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
                        }
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
                //String fileId = update.getMessage().getPhoto().get(0).getFileId();
                if(update.getMessage().getDocument()==null){
                    sendTextMessage("Файл должен быть БЕЗ СЖАТИЯ.");
                    return;
                }
                if(update.getMessage().getDocument().getFileSize()> 1_048_576){
                    sendTextMessage("Размер файла "+update.getMessage().getDocument().getFileSize() +"\n файл слишком велик." +
                            "загрузите новое изоюражение.");
                    return;
                }
                String fileId = update.getMessage().getDocument().getFileId();
                File file = execute(new org.telegram.telegrambots.meta.api.methods.GetFile(fileId));
                String filePath = file.getFilePath();
                byte[] photo = downloadNewFile(filePath);
                lostAnimal.setImageData(photo);
                if (user.getLostAnimals() == null) {
                    user.setLostAnimals(new ArrayList<>());
                }
                user.addLostAnimals(lostAnimal);
                lostAnimal.setUser(user);
                userService.saveUser(user);
                questionCount = 9;
                sendTextMessage("Это финальный пункт.\nВот ваша анкета: ");
                //sendPhotoAsFile(lostAnimal.getImageData(),update.getMessage().getChatId());
                sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getMessage().getChatId());
                sendHtmlMessage(""+user+ lostAnimal);
                return;
            }

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
            System.out.println(baos.size());
            return baos.toByteArray(); // Возвращаем массив байтов
        }
    }
    public void sendPhotoMessageFromByteArray(byte[] imageData,Long chatID) {
        // Преобразуем массив байтов в InputStream
        InputStream inputStream = new ByteArrayInputStream(imageData);

        // Отправляем фото
        try {
            // Используйте метод sendPhoto из вашей библиотеки Telegram
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatID); // Установите ID чата, куда отправляется фото
            sendPhoto.setPhoto(new InputFile(inputStream, "image.jpg")); // Укажите имя файла

            // Отправка сообщения
            execute(sendPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendPhotoAsFile(byte[] imageData, Long chatID) {
        InputStream inputStream = new ByteArrayInputStream(imageData);

        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatID); // Установите ID чата, куда отправляется фото
            sendDocument.setDocument(new InputFile(inputStream, "image.jpg")); // Укажите имя файла

            // Отправка сообщения
            execute(sendDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   /* public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }*/
}
