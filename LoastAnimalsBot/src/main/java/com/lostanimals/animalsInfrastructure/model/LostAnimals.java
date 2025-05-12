package com.lostanimals.animalsInfrastructure.model;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import jakarta.persistence.*;
/*
@Entity
@Table(name = "lost_animals")*/
public class LostAnimals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Уникальный идентификатор

    @Column(name = "type", nullable = false)
    private String type; // Тип животного (например, собака, кошка и т.д.)

    @Column(name = "name", nullable = false)
    private String name; // Имя животного

    @Column(name = "sex", nullable = false)
    private String sex; // Пол животного (например, мужской, женский)

    @Column(name = "age")
    private Integer age; // Возраст животного

    @Column(name = "city", nullable = false)
    private String city; // Город, где пропало животное

    @Column(name = "district")
    private String district; // Район, где пропало животное

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusType status; // Статус (например, найдено, пропало)

    @Column(name = "description")
    private String description; // Описание животного

    @Lob // Указывает, что это большой объект
    @Column(name = "imageData")
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Внешний ключ
    private User master;

    public LostAnimals(User master, String description, StatusType status, String district, String city, Integer age, String sex, String name, String type) {
        this.master = master;
        this.description = description;
        this.status = status;
        this.district = district;
        this.city = city;
        this.age = age;
        this.sex = sex;
        this.name = name;
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
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

    public void setMaster(User master) {
        this.master = master;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
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

    public User getMaster() {
        return master;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
