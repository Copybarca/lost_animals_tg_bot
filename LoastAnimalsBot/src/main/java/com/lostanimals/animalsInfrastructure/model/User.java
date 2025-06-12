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
    @Column(name = "tg_id",unique = true)
    private String tgId;
    @Column(name = "phone_number")
    private String phoneNumber;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LostAnimal> lostAnimals; // Связь с потерянными животными

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
    //@Transactional
    public List<LostAnimal> getLostAnimals() {
        return lostAnimals;
    }
    public void setLostAnimals(List<LostAnimal> lostAnimals) {
        this.lostAnimals = lostAnimals;
    }                                                                                 

    public void addLostAnimals(LostAnimal lostAnimal) {
        this.lostAnimals.add(lostAnimal);
    }
    @Override
    public String toString() {
        return "Ваш TG_ID: " + tgId + "\n Телефон, который вы оставили: " + phoneNumber+".\n";
    }
}
