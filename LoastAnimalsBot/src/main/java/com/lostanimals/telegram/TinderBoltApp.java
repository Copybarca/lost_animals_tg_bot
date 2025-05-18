package com.lostanimals.telegram;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.AnimalType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.SexType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.service.LostAnimalsService;
import com.lostanimals.animalsInfrastructure.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final LostAnimalsService lostAnimalsService;
    private final User user;
    private final LostAnimals lostAnimal;
    private  List<LostAnimals> lostAnimalsList;
    private int currentNumberOfAnimal = 0;
    private int page =0;
    public TinderBoltApp(ApplicationContext applicationContext) {
        super(Tokens.TELEGRAM_BOT_NAME, Tokens.TELEGRAM_BOT_TOKEN);
        this.applicationContext=applicationContext;
        this.userService = applicationContext.getBean(UserService.class);
        this.user = applicationContext.getBean(User.class);
        this.lostAnimal = applicationContext.getBean(LostAnimals.class);
        this.lostAnimalsService = applicationContext.getBean(LostAnimalsService.class);
    }
    @Override
    public void onUpdateEventReceived(Update update) throws Exception {
        //TODO: основной функционал бота будем писать здесь
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message ="";
            var data = update.getMessage();
            if(data!=null)
                message = data.getText()==null?"":data.getText();
            switch(message){// -- Начало меню выбора режима диалога --
                case "/start":
                    dialogMode = DialogMode.MAIN;
                    sendPhotoMessage("main");
                    sendHtmlMessage(loadMessage("main"));
                    showMainMenu(
                            "Начало","/start",
                            "Оставить заявку о потере ","/lost",
                            "Оставить заявку о нахождении ","/found",
                            "Посмотреть анкеты найденых ","/found_profiles",
                            "Посмотреть анкеты потерянных ","/lost_profiles",
                            "Мои анкеты ","/my_profiles"
                    );
                    return;
                case "/lost"://TODO: доработать ветку алгоритма
                    dialogMode = DialogMode.LOST;
                    sendPhotoMessage("lost");
                    questionCount = 0;
                    sendHtmlMessage("Вы можете составить анкету о пропаже вашего животного. Укажите свою контактную информацию.\nВаш TG_ID будет приписан анкете автоматически.");
                    sendTextMessage("Укажите телефон по желанию в формате 8xxxxxxxxxx");

                    //TODO: проверить номер на соответствие форме
                    return;
                case "/found"://TODO: доработать ветку алгоритма
                    dialogMode = DialogMode.FOUND;
                    sendPhotoMessage("found");
                    questionCount = 0;
                    sendHtmlMessage("Вы можете оставить заявку о том, что нашли животное, вероятно потерянное. Укажите свою контактную информацию.\nВаш TG_ID будет приписан анкете автоматически.");
                    sendTextMessage("Укажите телефон по желанию в формате 8xxxxxxxxxx");
                    return;
                case "/found_profiles":
                    dialogMode = DialogMode.SEE_FOUND;
                    lostAnimalsList = null;
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case "/lost_profiles":
                    dialogMode = DialogMode.SEE_LOST;
                    lostAnimalsList = null;
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case "/my_profiles":
                    dialogMode = DialogMode.SEE_MY;
                    User currentUser = userService.getUserByTgID(update.getMessage().getFrom().getUserName());
                    if(currentUser==null){
                        sendTextMessage("Похоже, у вас пока нет анкет");
                    }else if(currentUser.getLostAnimals()==null){
                        sendTextMessage("Похоже, у вас пока нет анкет");
                    }else{
                        List<LostAnimals> lostAnimalsByCurrentUserList = lostAnimalsService.getAllByUser(currentUser);
                        for(LostAnimals lostAnimal : lostAnimalsByCurrentUserList){
                            sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getMessage().getChatId());
                            sendHtmlMessage(lostAnimal.toString());
                        }
                    }
                    sendProfilesCommandKeyboard(update.getMessage().getChatId());
                    return;
                default:
                    break;
            }// -- Конец меню выбора режима диалога --

            switch(dialogMode){// -- Начало меню обработки режима диалога --
                case LOST:
                    if(questionCount==0){
                        String tgUsername = update.getMessage().getFrom().getUserName();
                        user.setTgId(tgUsername);
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
                        lostAnimal.setName(message);
                        sendSexTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }if(questionCount==3){
                        sendTextMessage("Введите район пропажи");
                        lostAnimal.setCity(message);
                        questionCount = 4;
                        return;
                    }if(questionCount==4){
                        sendTextMessage("Введите приметы животного в свободной форме");

                        lostAnimal.setDistrict(message);

                        StatusType animalsStatus = StatusType.LOST;
                        lostAnimal.setStatus(animalsStatus);

                        questionCount = 5;
                        return;
                    }if(questionCount==5){
                        sendTextMessage("Введите дату в формате гггг-мм-дд");
                        lostAnimal.setDescription(message);
                        questionCount = 6;
                        return;
                    }if(questionCount==6){
                        handleEnteredDate(message,"Прикрепите фото животного как ФАЙЛ БЕЗ СЖАТИЯ.\n Размер фото до 1МБ включительно.");
                        return;
                    }
                    break;
                case FOUND://TODO: доработать ветку алгоритма
                    if(questionCount==0){
                        String tgUsername = update.getMessage().getFrom().getUserName();
                        user.setTgId(tgUsername);
                        user.setPhoneNumber(message);
                        sendAnimalTypeKeyboard(update.getMessage().getChatId());
                        lostAnimal.setStatus(StatusType.FOUND);
                        return;
                    }if(questionCount==1){
                        lostAnimal.setCity(message);
                        sendTextMessage("Введите район, в котором нашли животное");
                        questionCount=2;
                        return;
                    }if(questionCount==2){
                        lostAnimal.setDistrict(message);
                        sendSexTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }
                    if(questionCount==3){
                        handleEnteredDate(message,"Введите приметы животного");
                        return;
                    }if(questionCount==4){
                        lostAnimal.setDescription(message);
                        sendTextMessage("Прикрепите фото животного как ФАЙЛ БЕЗ СЖАТИЯ.\n Размер фото до 1МБ включительно.");
                        return;
                    }
                    break;
                case SEE_LOST:
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case SEE_FOUND:
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case SEE_MY:
                    sendProfilesCommandKeyboard(update.getMessage().getChatId());
                    return;
                default:
                    break;
            }// -- Конец меню обработки режима диалога --

        }else if (update.hasCallbackQuery()) {// -- Начало меню обработки ответа кнопок --
            switch(dialogMode){
                case LOST:
                    if(questionCount==0){
                        handleAnimalTypeKeyboard(update,"Сколько лет животному");
                    }else if(questionCount==2){
                        handleAnimalSexKeyboard(update,"Введите город пропажи");
                    }
                case FOUND:
                    if(questionCount==0){
                        handleAnimalTypeKeyboard(update,"Введите город нахождения");
                    }else if(questionCount==2){
                        handleAnimalSexKeyboard(update,"Введите дату нахождения животного в формате гггг-мм-дд");
                    }
                    return;
                case SEE_LOST:
                    handleUserResponseToShowAnimalsByStatus(update,StatusType.LOST);
                    return;
                case SEE_FOUND:
                    handleUserResponseToShowAnimalsByStatus(update,StatusType.FOUND);
                    return;
                case SEE_MY:
                    handleProfilesCommandKeyboard(update,"");
                    return;
                default:
                    break;
            }
        }else if (update.hasMessage()) {// -- Начало меню обработки отправки пользователем файлов --
            switch(dialogMode) {
                case LOST:
                    if(questionCount==7) {
                        //String fileId = update.getMessage().getPhoto().get(0).getFileId();
                        if(userService.getUserByTgID(user.getTgId())!=null){
                            lostAnimalsService.addAnimalForUser(user,lostAnimal);
                        }else{
                            handleUserSendDocument(update,"Это финальный пункт.\nВот ваша анкета: ");
                            if (user.getLostAnimals() == null) {
                                user.setLostAnimals(new ArrayList<>());
                            }
                            user.addLostAnimals(lostAnimal);
                            lostAnimal.setUser(user);
                            userService.saveUser(user);
                        }
                        sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getMessage().getChatId());
                        sendHtmlMessage(""+user+ lostAnimal);
                        return;
                    }
                case FOUND:
                    if(questionCount==4){
                        handleUserSendDocument(update,"Это финальный пункт.\nВот ваша анкета: ");
                        if(userService.getUserByTgID(user.getTgId())!=null){
                            lostAnimalsService.addAnimalForUser(user,lostAnimal);
                        }else{
                            if (user.getLostAnimals() == null) {
                                user.setLostAnimals(new ArrayList<>());
                            }
                            user.addLostAnimals(lostAnimal);
                            lostAnimal.setUser(user);
                            userService.saveUser(user);
                        }
                        sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getMessage().getChatId());
                        sendHtmlMessage(""+user+ lostAnimal);
                        return;
                    }
                    return;
                default:
                    break;
            }

        }
    }
    private void handleUserResponseToShowAnimalsByStatus(Update update,StatusType statusType){
        String nextStr = update.getCallbackQuery().getData();
        switch (nextStr){
            case "Показать анкету":
                int size = 5;
                if(lostAnimalsList == null || lostAnimalsList.isEmpty()){
                    PageRequest pageRequest = PageRequest.of(page,size, Sort.unsorted());
                    lostAnimalsList = lostAnimalsService.getAllLostByStatusPartly(statusType,pageRequest);
                    if(lostAnimalsList==null || lostAnimalsList.isEmpty()){
                        sendTextMessage("Тут пока нет новых анкет");
                        page=0;
                        currentNumberOfAnimal=0;
                        return;
                    }
                    LostAnimals lostAnimal = lostAnimalsList.get(currentNumberOfAnimal);
                    sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getCallbackQuery().getMessage().getChatId());
                    sendHtmlMessage(lostAnimal.toStringForFoundOrLostPage());
                    currentNumberOfAnimal+=1;
                    sendNextSwitcherKeyboard(update.getCallbackQuery().getMessage().getChatId());
                }else if(currentNumberOfAnimal == lostAnimalsList.size()){
                    currentNumberOfAnimal=0;
                    page+=1;
                    PageRequest pageRequest = PageRequest.of(page,size, Sort.unsorted());
                    lostAnimalsList = lostAnimalsService.getAllLostByStatusPartly(statusType,pageRequest);
                    if(lostAnimalsList==null || lostAnimalsList.isEmpty()){
                        sendTextMessage("Тут пока нет новых анкет");
                        page=0;
                        currentNumberOfAnimal=0;
                        return;
                    }else{
                        LostAnimals lostAnimal = lostAnimalsList.get(currentNumberOfAnimal);
                        sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getCallbackQuery().getMessage().getChatId());
                        sendHtmlMessage(lostAnimal.toStringForFoundOrLostPage());
                        currentNumberOfAnimal+=1;
                        sendNextSwitcherKeyboard(update.getCallbackQuery().getMessage().getChatId());
                    }
                }else{
                    LostAnimals lostAnimal = lostAnimalsList.get(currentNumberOfAnimal);
                    sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getCallbackQuery().getMessage().getChatId());
                    sendHtmlMessage(lostAnimal.toStringForFoundOrLostPage());
                    currentNumberOfAnimal+=1;
                    sendNextSwitcherKeyboard(update.getCallbackQuery().getMessage().getChatId());
                }
            default:
                break;
        }
    }
    private void handleUserSendDocument(Update update,String nextMessage) throws Exception {
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
        questionCount +=1;
        sendTextMessage(nextMessage);
        //sendPhotoAsFile(lostAnimal.getImageData(),update.getMessage().getChatId());

    }
    private void handleAnimalSexKeyboard(Update update,String nextMessage){
        String sexType = update.getCallbackQuery().getData();

        switch (sexType) {
            case "Девочка":
                lostAnimal.setSex(SexType.FEMALE);
                break;
            case "Мальчик":
                lostAnimal.setSex(SexType.MALE);
                break;
            default:
                sendTextMessage("Введены невалидные данные, выберите пункт кнопки заново");
                sendAnimalTypeKeyboard(update.getMessage().getChatId());
                return;
        }
        sendTextMessage("Вы выбрали: " + sexType);
        sendTextMessage(nextMessage);
        this.questionCount += 1; // Переход к следующему вопросу

    }
    private void handleAnimalTypeKeyboard(Update update,String nextMessage){
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
            sendTextMessage(nextMessage);
            this.questionCount +=1; // Переход к следующему вопросу
        }
    }
    private void sendSexTypeKeyboard(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Девочка").callbackData("Девочка").build());
        row.add(InlineKeyboardButton.builder().text("Мальчик").callbackData("Мальчик").build());
        buttons.add(row);

        markup.setKeyboard(buttons);
        sendMessageWithKeyboard(chatId, "Выберите пол животного:", markup);
    }
    private void handleProfilesCommandKeyboard(Update update,String nextMessage){
        String stringCommand = update.getCallbackQuery().getData();
        sendTextMessage("Вы выбрали: " + stringCommand);
        switch (stringCommand) {
            case "Удалить все анкеты":
                User userToDelete = userService.getUserByTgID(update.getMessage().getFrom().getUserName());
                lostAnimalsService.deleteLostAnimalsByUser(userToDelete);
                break;
            case "Создать новую анкету о потере":
                Message messageLost = new Message();
                messageLost.setText("/lost");
                Update updateLost = new Update();
                updateLost.setMessage(messageLost);
                try {
                    onUpdateEventReceived(updateLost);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Создать новую анкету о нахождении":
                Message messageFound = new Message();
                messageFound.setText("/found");
                Update updateFound = new Update();
                updateFound.setMessage(messageFound);
                try {
                    onUpdateEventReceived(updateFound);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                sendTextMessage("Введены невалидные данные, выберите пункт кнопки заново или отправьте валидные запросы из пункта меню.");
                sendAnimalTypeKeyboard(update.getMessage().getChatId());
                return;
        }
        sendTextMessage(nextMessage);
        this.questionCount += 1; // Переход к следующему вопросу
    }
    private void sendProfilesCommandKeyboard(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Удалить все анкеты").callbackData("Удалить все анкеты").build());
        buttons.add(row);
        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Создать новую анкету о потере").callbackData("Создать новую анкету о потере").build());
        buttons.add(row);
        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Создать новую анкету о нахождении").callbackData("Создать новую анкету о нахождении").build());
        buttons.add(row);
        markup.setKeyboard(buttons);
        sendMessageWithKeyboard(chatId, "Выберите желаемое действие:", markup);
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
    private void sendNextSwitcherKeyboard(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Показать анкету").callbackData("Показать анкету").build());
        buttons.add(row);

        markup.setKeyboard(buttons);
        sendMessageWithKeyboard(chatId, "Для просмотра следующих анкет нажмите далее", markup);
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
    private void handleEnteredDate(String message,String nextMessage){
        String[] arr = message.split("-");
        if(arr.length<3){
            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
            return;
        }
        java.sql.Date date;
        try{
            date = Date.valueOf(message);
            lostAnimal.setDate(date);
            questionCount +=1;
            sendTextMessage(nextMessage);
        }catch (IllegalArgumentException e){
            e.printStackTrace();//TODO: тут можно добавить сервис для логирования потом
            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
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
