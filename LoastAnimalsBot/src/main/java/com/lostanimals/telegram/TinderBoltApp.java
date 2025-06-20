package com.lostanimals.telegram;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.AnimalType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.SexType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimal;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.model.UserSession;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TinderBoltApp extends MultiSessionTelegramBot {

    private final UserService userService;
    private final LostAnimalsService lostAnimalsService;
    private AbstractMap<Long, UserSession> userSessions;

    public TinderBoltApp(ApplicationContext applicationContext) {
        super(Tokens.TELEGRAM_BOT_NAME, Tokens.TELEGRAM_BOT_TOKEN);
        this.userService = applicationContext.getBean(UserService.class);
        this.lostAnimalsService = applicationContext.getBean(LostAnimalsService.class);
        this.userSessions = new ConcurrentHashMap<>();
    }
    @Override
    public void onUpdateEventReceived(Update update) throws Exception {

        if(update.hasMessage() && update.getMessage().getChatId()!=null){
            userSessions.computeIfAbsent(update.getMessage().getChatId(), id ->
                    new UserSession(DialogMode.MOCK, new User(), new LostAnimal(), 0, new ArrayList<>(), 0, 0));
        }

        //TODO: основной функционал бота будем писать здесь
         if (update.hasMessage() && update.getMessage().hasText()) {//TODO: вот тут не проверяется chatId - подумать а должно ли тут эт осостояние проверяться
            String message ="";
            var data = update.getMessage();
            if(data!=null)
                message = data.getText()==null?"":data.getText();
            switch(message){// -- Начало меню выбора режима диалога --
                case "/start":
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.MAIN);
                    sendPhoto("main",update.getMessage().getChatId());
                    sendHtmlMessage(getMessagesText("main"));
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
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.LOST);
                    sendPhoto("lost",update.getMessage().getChatId());
                    userSessions.get(update.getMessage().getChatId()).setQuestionCount(0);
                    sendHtmlMessage("Вы можете составить анкету о пропаже вашего животного. Укажите свою контактную информацию.\nВаш TG_ID будет приписан анкете автоматически.");
                    sendTextMessage("Укажите телефон по желанию в формате 8xxxxxxxxxx");

                    //TODO: проверить номер на соответствие форме
                    return;
                case "/found"://TODO: доработать ветку алгоритма
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.FOUND);
                    sendPhoto("found",update.getMessage().getChatId());
                    userSessions.get(update.getMessage().getChatId()).setQuestionCount(0);
                    sendHtmlMessage("Вы можете оставить заявку о том, что нашли животное, вероятно потерянное. Укажите свою контактную информацию.\nВаш TG_ID будет приписан анкете автоматически.");
                    sendTextMessage("Укажите телефон по желанию в формате 8xxxxxxxxxx");
                    return;
                case "/found_profiles":
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.SEE_FOUND);
                    userSessions.get(update.getMessage().getChatId()).setLostAnimalsList(null);
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case "/lost_profiles":
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.SEE_LOST);
                    userSessions.get(update.getMessage().getChatId()).setLostAnimalsList(null);
                    sendNextSwitcherKeyboard(update.getMessage().getChatId());
                    return;
                case "/my_profiles":
                    userSessions.get(update.getMessage().getChatId()).setDialogMode(DialogMode.SEE_MY);
                    User currentUser = userService.getUserByTgID(update.getMessage().getFrom().getUserName());
                    if(currentUser==null){
                        sendTextMessage("Похоже, у вас пока нет анкет");
                    }else if(currentUser.getLostAnimals()==null){
                        sendTextMessage("Похоже, у вас пока нет анкет");
                    }else{
                        List<LostAnimal> lostAnimalByCurrentUserList = lostAnimalsService.getAllByUser(currentUser);
                        for(LostAnimal lostAnimal : lostAnimalByCurrentUserList){
                            sendPhotoMessageFromByteArray(lostAnimal.getImageData(),update.getMessage().getChatId());
                            sendHtmlMessage(lostAnimal.toString());
                        }
                    }
                    sendProfilesCommandKeyboard(update.getMessage().getChatId());
                    return;
                default:
                    break;
            }// -- Конец меню выбора режима диалога --

            switch(userSessions.get(update.getMessage().getChatId()).getDialogMode()){// -- Начало меню обработки режима диалога --
                case LOST:
                    if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==0){
                        String tgUsername = update.getMessage().getFrom().getUserName();
                        User currentUser = userSessions.get(update.getMessage().getChatId()).getUser();
                        currentUser.setPhoneNumber(message);
                        currentUser.setTgId(tgUsername);
                        sendAnimalTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==1){
                        sendTextMessage("Введите кличку животного");
                        int animalsAge = Integer.parseInt(message);
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setAge(animalsAge);
                        userSessions.get(update.getMessage().getChatId()).setQuestionCount(2);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==2){
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setName(message);
                        sendSexTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==3){
                        sendTextMessage("Введите район пропажи");
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setCity(message);
                        userSessions.get(update.getMessage().getChatId()).setQuestionCount(4);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==4){
                        sendTextMessage("Введите приметы животного в свободной форме");

                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setDistrict(message);

                        StatusType animalsStatus = StatusType.LOST;
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setStatus(animalsStatus);

                        userSessions.get(update.getMessage().getChatId()).setQuestionCount(5);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==5){
                        sendTextMessage("Введите дату в формате гггг-мм-дд");
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setDescription(message);
                        userSessions.get(update.getMessage().getChatId()).setQuestionCount(6);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==6){
                        handleEnteredDate(message,"Прикрепите фото животного как ФАЙЛ БЕЗ СЖАТИЯ.\n Размер фото до 1МБ включительно.",update);
                        return;
                    }
                    break;
                case FOUND:
                    if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==0){
                        String tgUsername = update.getMessage().getFrom().getUserName();
                        User currentUser = userSessions.get(update.getMessage().getChatId()).getUser();
                        currentUser.setPhoneNumber(message);
                        currentUser.setTgId(tgUsername);
                        sendAnimalTypeKeyboard(update.getMessage().getChatId());
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setStatus(StatusType.FOUND);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==1){
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setCity(message);
                        sendTextMessage("Введите район, в котором нашли животное");
                        userSessions.get(update.getMessage().getChatId()).setQuestionCount(2);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==2){
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setDistrict(message);
                        sendSexTypeKeyboard(update.getMessage().getChatId());
                        return;
                    }
                    if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==3){
                        handleEnteredDate(message,"Введите приметы животного",update);
                        return;
                    }if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==4){
                        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setDescription(message);
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
            switch(userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getDialogMode()){
                case LOST:
                    if(userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getQuestionCount()==0){
                        handleAnimalTypeKeyboard(update,"Сколько лет животному");
                    }else if(userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getQuestionCount()==2){
                        handleAnimalSexKeyboard(update,"Введите город пропажи");
                    }
                case FOUND:
                    if(userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getQuestionCount()==0){
                        handleAnimalTypeKeyboard(update,"Введите город нахождения");
                    }else if(userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getQuestionCount()==2){
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
                    handleProfilesCommandKeyboard(update,"Можете продолжить");
                    return;
                default:
                    break;
            }
        }else if (update.hasMessage()) {// -- Начало меню обработки отправки пользователем файлов --
            switch(userSessions.get(update.getMessage().getChatId()).getDialogMode()) {
                case LOST:
                    if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==7) {
                        //String fileId = update.getMessage().getPhoto().get(0).getFileId();
                        handleUserSendDocument(update,"Это финальный пункт.\nВот ваша анкета: ");
                        User user = userSessions.get(update.getMessage().getChatId()).getUser();
                        if(userService.getUserByTgID(user.getTgId())!=null){
                            lostAnimalsService.addAnimalForUser(user,userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                        }else{
                            if (user.getLostAnimals() == null) {
                                user.setLostAnimals(new ArrayList<>());
                            }
                            user.addLostAnimals(userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                            userSessions.get(update.getMessage().getChatId()).getLostAnimal().setUser(user);
                            userService.saveUser(user);
                        }
                        sendPhotoMessageFromByteArray(userSessions.get(update.getMessage().getChatId()).getLostAnimal().getImageData(),update.getMessage().getChatId());
                        sendHtmlMessage(""+user+ userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                        return;
                    }
                case FOUND:
                    if(userSessions.get(update.getMessage().getChatId()).getQuestionCount()==4){
                        User user = userSessions.get(update.getMessage().getChatId()).getUser();
                        handleUserSendDocument(update,"Это финальный пункт.\nВот ваша анкета: ");
                        if(userService.getUserByTgID(user.getTgId())!=null){
                            lostAnimalsService.addAnimalForUser(user,userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                        }else{
                            if (user.getLostAnimals() == null) {
                                user.setLostAnimals(new ArrayList<>());
                            }
                            user.addLostAnimals(userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                            userSessions.get(update.getMessage().getChatId()).getLostAnimal().setUser(user);
                            userService.saveUser(user);
                        }
                        sendPhotoMessageFromByteArray(userSessions.get(update.getMessage().getChatId()).getLostAnimal().getImageData(),update.getMessage().getChatId());
                        sendHtmlMessage(""+user+ userSessions.get(update.getMessage().getChatId()).getLostAnimal());
                        return;
                    }
                    return;
                default:
                    break;
            }

        }
    }
    private void handleUserResponseToShowAnimalsByStatus(Update update, StatusType statusType) {
        String nextStr = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        UserSession userSession = userSessions.get(chatId);
        int size = 5;

        switch (nextStr) {
            case "Показать анкету":
                showAnimalProfile(userSession, statusType, chatId, size);
                break;
            default:
                break;
        }
    }

    private void showAnimalProfile(UserSession userSession, StatusType statusType, long chatId, int size) {
        if (userSession.getLostAnimalsList() == null || userSession.getLostAnimalsList().isEmpty()) {
            loadLostAnimals(userSession, statusType, size);
        }

        if (userSession.getLostAnimalsList() == null || userSession.getLostAnimalsList().isEmpty()) {
            sendTextMessage("Тут пока нет новых анкет");
            resetUserSession(userSession);
            return;
        }

        if (userSession.getCurrentNumberOfAnimal() >= userSession.getLostAnimalsList().size()) {
            userSession.setCurrentNumberOfAnimal(0);
            userSession.addPage();
            loadLostAnimals(userSession, statusType, size);

            if (userSession.getLostAnimalsList() == null || userSession.getLostAnimalsList().isEmpty()) {
                sendTextMessage("Тут пока нет новых анкет");
                resetUserSession(userSession);
                return;
            }
        }

        sendCurrentAnimalProfile(userSession, chatId);
    }

    private void loadLostAnimals(UserSession userSession, StatusType statusType, int size) {
        PageRequest pageRequest = PageRequest.of(userSession.getPage(), size, Sort.unsorted());
        userSession.setLostAnimalsList(lostAnimalsService.getAllLostByStatusPartly(statusType, pageRequest));
    }

    private void sendCurrentAnimalProfile(UserSession userSession, long chatId) {
        LostAnimal lostAnimal = userSession.getLostAnimalsList().get(userSession.getCurrentNumberOfAnimal());
        sendPhotoMessageFromByteArray(lostAnimal.getImageData(), chatId);
        sendHtmlMessage(lostAnimal.getUser().toString() + lostAnimal.toStringForFoundOrLostPage());
        userSession.currentNumberOfAnimalAdd();
        sendNextSwitcherKeyboard(chatId);
    }

    private void resetUserSession(UserSession userSession) {
        userSession.setPage(0);
        userSession.setCurrentNumberOfAnimal(0);
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
        userSessions.get(update.getMessage().getChatId()).getLostAnimal().setImageData(photo);
        userSessions.get(update.getMessage().getChatId()).questionCountAdd();
        sendTextMessage(nextMessage);
        //sendPhotoAsFile(lostAnimal.getImageData(),update.getMessage().getChatId());

    }
    private void handleAnimalSexKeyboard(Update update,String nextMessage){
        String sexType = update.getCallbackQuery().getData();

        switch (sexType) {
            case "Девочка":
                userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getLostAnimal().setSex(SexType.FEMALE);
                break;
            case "Мальчик":
                userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getLostAnimal().setSex(SexType.MALE);
                break;
            default:
                sendTextMessage("Введены невалидные данные, выберите пункт кнопки заново");
                sendAnimalTypeKeyboard(update.getCallbackQuery().getMessage().getChatId());
                return;
        }
        sendTextMessage("Вы выбрали: " + sexType);
        sendTextMessage(nextMessage);
        userSessions.get(update.getCallbackQuery().getMessage().getChatId()).questionCountAdd(); // Переход к следующему вопросу

    }
    private void handleAnimalTypeKeyboard(Update update,String nextMessage){
        String animalsType = update.getCallbackQuery().getData();
        boolean isReady = false;
        switch (animalsType) {
            case "Кошка":
                userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getLostAnimal().setType(AnimalType.CAT);
                isReady = true;
                break;
            case "Собака":
                userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getLostAnimal().setType(AnimalType.DOG);
                isReady = true;
                break;
            case "Птица":
                userSessions.get(update.getCallbackQuery().getMessage().getChatId()).getLostAnimal().setType(AnimalType.PARROT);
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
            userSessions.get(update.getCallbackQuery().getMessage().getChatId()).questionCountAdd(); // Переход к следующему вопросу
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
    private void deleteAllByUserId(Update update){
        synchronized (this){
            User userToDelete = userService.getUserByTgID(update.getCallbackQuery().getFrom().getUserName());
            lostAnimalsService.deleteAllLostAnimalsByUser(userToDelete);
        }
    }
    private void handleProfilesCommandKeyboard(Update update,String nextMessage){
        String stringCommand = update.getCallbackQuery().getData();
        sendTextMessage("Вы выбрали: " + stringCommand);
        Chat chat = new Chat(update.getCallbackQuery().getMessage().getChatId(),"user");
        switch (stringCommand) {
            case "Удалить все анкеты":
                deleteAllByUserId(update);
                break;
            case "Создать новую анкету о потере":
                Message messageLost = new Message();
                messageLost.setText("/lost");
                messageLost.setChat(chat);
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
                messageFound.setChat(chat);
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
        userSessions.get(update.getCallbackQuery().getMessage().getChatId()).questionCountAdd(); // Переход к следующему вопросу
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
        try (InputStream inputStream = new ByteArrayInputStream(imageData)){
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
    private void handleEnteredDate(String message,String nextMessage,Update update){
        String[] arr = message.split("-");
        if(arr.length<3){
            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
            return;
        }
        java.sql.Date date;
        try{
            date = Date.valueOf(message);
            userSessions.get(update.getMessage().getChatId()).getLostAnimal().setDate(date);
            userSessions.get(update.getMessage().getChatId()).questionCountAdd();
            sendTextMessage(nextMessage);
        }catch (IllegalArgumentException e){
            e.printStackTrace();//TODO: тут можно добавить сервис для логирования потом
            sendTextMessage("Невалидная дата. Введите дату в формате гггг-мм-дд.");
        }
    }
    public void sendPhotoAsFile(byte[] imageData, Long chatID) {
        try(InputStream inputStream = new ByteArrayInputStream(imageData);) {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatID); // Установите ID чата, куда отправляется фото
            sendDocument.setDocument(new InputFile(inputStream, "image.jpg")); // Укажите имя файла
            // Отправка сообщения
            execute(sendDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendPhoto(String name,Long chatId) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/"+name+".jpg");
             ByteArrayOutputStream baos = new ByteArrayOutputStream()){
             byte[] buffer = new byte[1024];
             int bytesRead;
             while ((bytesRead = inputStream.read(buffer)) != -1) {
                 baos.write(buffer, 0, bytesRead);
             }
             sendPhotoMessageFromByteArray(baos.toByteArray(),chatId);
             System.out.println(baos.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getMessagesText(String name) {
        String str = "";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("messages/"+name+".txt");
             ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            str = baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
}
