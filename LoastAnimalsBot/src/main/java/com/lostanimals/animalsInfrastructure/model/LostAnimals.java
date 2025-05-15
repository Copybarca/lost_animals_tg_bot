package com.lostanimals.animalsInfrastructure.model;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.AnimalType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.SexType;
import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Arrays;

@Entity
@Table(name = "lost_animals")
@Component
public class LostAnimals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Уникальный идентификатор

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AnimalType type; // Тип животного (например, собака, кошка и т.д.)

    @Column(name = "name")
    private String name; // Имя животного

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private SexType sex; // Пол животного (например, мужской, женский)

    @Column(name = "age")
    private Integer age; // Возраст животного

    @Column(name = "city")
    private String city; // Город, где пропало животное

    @Column(name = "district")
    private String district; // Район, где пропало животное

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusType status; // Статус (например, найдено, пропало)

    @Column(name = "description")
    private String description; // Описание животного

    @Column(name = "date")
    private Date date;
    @Lob // Указывает, что это большой объект
    @Column(name = "imageData")
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Внешний ключ
    private User user;

    public LostAnimals(User user, String description, StatusType status, String district, String city, Integer age, SexType sex, String name, AnimalType type) {
        this.user = user;
        this.description = description;
        this.status = status;
        this.district = district;
        this.city = city;
        this.age = age;
        this.sex = sex;
        this.name = name;
        this.type = type;
    }
    @Deprecated
    public LostAnimals() {}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(AnimalType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(SexType sex) {
        this.sex = sex;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser(User master) {
        this.user = master;
    }

    public Long getId() {
        return id;
    }

    public AnimalType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public SexType getSex() {
        return sex;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public User getUser() {
        return user;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public String toString() {
        return "Ваше животное: " +
                "\nТип: " + type +
                "\nИмя: " + name + '\'' +
                "\nПол: " + sex + '\'' +
                "\nВозраст: " + age +
                "\nГород: " + city + '\'' +
                "\nРайон: " + district + '\'' +
                "\nСтатус: " + status +
                "\nОписание: " + description + '\'' +
                "\nДата: " + date+".\n";
    }
    public String toStringForFoundOrLostPage() {
        return "Животное: " +
                "\nТип: " + type +
                "\nИмя: " + name + '\'' +
                "\nПол: " + sex + '\'' +
                "\nВозраст: " + age +
                "\nГород: " + city + '\'' +
                "\nРайон: " + district + '\'' +
                "\nСтатус: " + status +
                "\nОписание: " + description + '\'' +
                "\nДата: " + date+".\n";
    }
}
