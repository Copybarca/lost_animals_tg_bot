package com.lostanimals.animalsInfrastructure.model;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Entity
@Table(name = "users")
@Component
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tgId;
    private String phoneNumber;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LostAnimals> lostAnimals; // Связь с потерянными животными

    public User(String tgId, String phoneNumber) {
        this.tgId = tgId;
        this.phoneNumber = phoneNumber;
    }
    @Deprecated
    public User() {}

    public void setTgId(String tgId) {
        this.tgId = tgId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public String getTgId() {
        return tgId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public List<LostAnimals> getLostAnimals() {
        return lostAnimals;
    }
    public void setLostAnimals(List<LostAnimals> lostAnimals) {
        this.lostAnimals = lostAnimals;
    }
    public void addLostAnimals(LostAnimals lostAnimals) {
        this.lostAnimals.add(lostAnimals);
    }
    @Override
    public String toString() {
        return "Ваш TG_ID: " + tgId + "\n Телефон, который вы оставили: " + phoneNumber+".\n";
    }
}
