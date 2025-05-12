package com.lostanimals.telegram;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {

    private ChatGPTService chatGPT = new ChatGPTService(Tokens.OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;
    private UserInfo me;
    private int questionCount;
    private ArrayList<String> messageList = new ArrayList<>();

    private User user;
    private LostAnimals lostAnimal = new LostAnimals(null,null,null,"","",0,null,null,"");



    public TinderBoltApp() {
        super(Tokens.TELEGRAM_BOT_NAME, Tokens.TELEGRAM_BOT_TOKEN);
    }
    @Override
    public void onUpdateEventReceived(Update update) throws Exception {
        //TODO: основной функционал бота будем писать здесь
        var data = update.getMessage();
        String message = data.getText()==null?"":data.getText();
        switch(message){
            case "/start":
                dialogMode = DialogMode.MAIN;
                sendPhotoMessage("main");
                sendTextMessage(loadMessage("main"));
                showMainMenu(
                        "Начало","/start",
                        "Оставить заявку о потере \uD83D\uDE0E","/lost",
                        "Оставить заявку о нахождении \uD83E\uDD70","/found",
                        "Посмотреть анкеты найденых \uD83D\uDE08","/found_profiles",
                        "Мои анкеты \uD83D\uDD25","/my_profiles"
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
                sendPhotoMessage("opener");
                sendTextMessage("Расскажи о девушке, чтобы генеарция была более подходящей");
                message = getMessageText();
                return;
            case "/found_profiles":
                dialogMode = DialogMode.MOCK;
                sendPhotoMessage("message");
                sendTextButtonsMessage("Пришлите в чат переписку",
                        "Следующее сообщение ","message_next",
                        "Пригласить на свидание","message_date");
                sendTextMessage("Пришли в чат свою переписку");
                message = getMessageText();
                return;
            case "/my_profiles":
                dialogMode = DialogMode.MOCK;
                sendPhotoMessage("date");
                sendTextButtonsMessage(loadMessage("date"),
                        "Ариана Гранде","date_grande",
                        "Райна Гослинг","date_gosling",
                        "Марго Робби","date_robbie",
                        "Зендая","date_zendaya",
                        "Мистер Хардли","date_hardly");
                return;
            default:
                break;
        }

        switch(dialogMode){
            case LOST://TODO: доработать ветку алгоритма
                String userTgId="";
                String userNumber="";

                String animalsType="";
                String animalsName="";
                String animalsSex="";
                int animalsAge=0;
                String animalsCity="";
                String animalsDistrict="";
                StatusType animalsStatus;
                String animalsDescription="";

                userTgId = String.valueOf(update.getMessage().getFrom().getId());
                if(questionCount==0){
                    userNumber = message;
                    questionCount = 1;
                    user = new User(userTgId,userNumber);
                    sendTextMessage("Введите вид животного");
                    return;
                }
                if(questionCount==1){
                    sendTextMessage("Сколько лет животному");
                    animalsType = message;
                    lostAnimal.setType(animalsType);
                    questionCount = 2;
                    return;
                }if(questionCount==2){
                    sendTextMessage("Введите кличку животного");
                    animalsAge= Integer.parseInt(message);
                    lostAnimal.setAge(animalsAge);
                    questionCount = 3;
                    return;
                }if(questionCount==3){
                    sendTextMessage("Введите пол животного");
                    animalsName = message;
                    lostAnimal.setName(animalsName);
                    questionCount = 4;
                    return;
                }if(questionCount==4){
                    sendTextMessage("Введите город пропажи");
                    animalsSex = message;
                    lostAnimal.setSex(animalsSex);
                    questionCount = 5;
                    return;
                }if(questionCount==5){
                    sendTextMessage("Введите район пропажи");
                    animalsCity = message;
                    lostAnimal.setCity(animalsCity);
                    questionCount = 6;
                    return;
                }if(questionCount==6){
                    sendTextMessage("Введите приметы животного в свободной форме");

                    animalsDistrict = message;
                    lostAnimal.setDistrict(animalsDistrict);

                    animalsStatus = StatusType.LOST;
                    lostAnimal.setStatus(animalsStatus);

                    questionCount = 7;
                    return;
                }if(questionCount==7){
                    sendTextMessage("Прикрепите фото животного");
                    animalsDescription = message;
                    lostAnimal.setDescription(animalsDescription);
                    questionCount = 8;
                    return;
                }if(questionCount==8) {
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
                break;
            case FOUND://TODO: доработать ветку алгоритма
                break;
            default:
                break;
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
