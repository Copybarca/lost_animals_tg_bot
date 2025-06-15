package com.lostanimals.animalsInfrastructure.model;

import com.lostanimals.telegram.DialogMode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

public class UserSession {
    private DialogMode dialogMode;// TODO: обезпасить в многопоточке
    private final User user;// TODO: обезпасить в многопоточке
    private LostAnimal lostAnimal;// TODO: обезпасить в многопоточке
    private int questionCount;// TODO: обезпасить в многопоточке
    private List<LostAnimal> lostAnimalList;// TODO: обезпасить в многопоточке
    private int currentNumberOfAnimal = 0; // TODO: обезпасить в многопоточке
    private int page =0; // TODO: обезпасить в многопоточке
    /**
     * Конструктор для создания объекта UserSession.
     *
     * @param dialogMode Режим диалога (например, поиск, добавление животного).
     * @param user Пользователь, которому принадлежит сессия.
     * @param lostAnimal  Информация о потерянном животном, с которым работает пользователь (может быть null).
     * @param questionCount Количество вопросов, заданных пользователю.
     * @param lostAnimalList Список всех потерянных животных (для отображения или выбора).  Может быть пустым.
     * @param currentNumberOfAnimal Текущий номер животного в списке lostAnimalsList, на котором находится пользователь.
     * @param page Номер страницы списка животных, если список большой.
     *
     * @throws IllegalArgumentException Если какие-либо входные параметры некорректны.  (Например, negative questionCount or currentNumberOfAnimal, page < 0)
     *
     *  Важно:  Проверьте валидность входных данных в конструкторе, чтобы избежать проблем в дальнейшем.
     */
    public UserSession(DialogMode dialogMode, User user, LostAnimal lostAnimal, int questionCount, List<LostAnimal> lostAnimalList, int currentNumberOfAnimal, int page) {
        this.dialogMode = dialogMode;
        this.user = user;
        this.lostAnimal = lostAnimal;
        this.questionCount = questionCount;
        this.lostAnimalList = lostAnimalList;
        this.currentNumberOfAnimal = currentNumberOfAnimal;
        this.page = page;
    }
    /**
     * Увеличивает счетчик страниц
     *
     * Этот метод используется для прибавления количества страниц,
     * которые были выгружены из бд. Каждый раз, когда вызывается
     * этот метод, значение переменной {@code page} увеличивается
     * на 1.
     *
     * @see #page
     */
    public void addPage(){
        page+=1;
    }
    /**
     * Увеличивает счетчик животных в списке пагинации на единицу.
     *
     * Этот метод используется для отслеживания количества просмотренных анкет на странице,
     * которые были выгружены из бд. Каждый раз, когда вызывается
     * этот метод, значение переменной {@code currentNumberOfAnimal} увеличивается
     * на 1.
     *
     * @see #currentNumberOfAnimal
     */
    public void currentNumberOfAnimalAdd(){
        currentNumberOfAnimal++;
    }
    /**
     * Увеличивает счетчик вопросов на единицу.
     *
     * Этот метод используется для отслеживания количества вопросов,
     * которые были заданы или обработаны. Каждый раз, когда вызывается
     * этот метод, значение переменной {@code questionCount} увеличивается
     * на 1.
     *
     * @see #questionCount
     */
    public void questionCountAdd(){
        questionCount++;
    }
    public void setDialogMode(DialogMode dialogMode) {
        this.dialogMode = dialogMode;
    }

    public void setLostAnimal(LostAnimal lostAnimal) {
        this.lostAnimal = lostAnimal;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public void setLostAnimalsList(List<LostAnimal> lostAnimalList) {
        this.lostAnimalList = lostAnimalList;
    }

    public void setCurrentNumberOfAnimal(int currentNumberOfAnimal) {
        this.currentNumberOfAnimal = currentNumberOfAnimal;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public DialogMode getDialogMode() {
        return dialogMode;
    }

    public User getUser() {
        return user;
    }

    public LostAnimal getLostAnimal() {
        return lostAnimal;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public List<LostAnimal> getLostAnimalsList() {
        return lostAnimalList;
    }

    public int getCurrentNumberOfAnimal() {
        return currentNumberOfAnimal;
    }

    public int getPage() {
        return page;
    }
}
