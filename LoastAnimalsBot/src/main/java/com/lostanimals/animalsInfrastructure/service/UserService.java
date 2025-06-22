package com.lostanimals.animalsInfrastructure.service;

import com.lostanimals.animalsInfrastructure.model.LostAnimal;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LostAnimal> getLostAnimals(String tgId) {
        User user = userRepository.findByTgId(tgId);
        return user.getLostAnimals();
    }
    public  User getUserByTgID(String tgId) {
        return userRepository.findByTgId(tgId);
    }

    private static ReentrantLock lock = new ReentrantLock();

    @Modifying
    public  void saveUser(User user) {
        lock.lock();
        try {
            userRepository.save(user);
        }finally {
            lock.unlock();
        }
    }
    @Modifying
    @Transactional
    public  void updateUser(User user) {
        lock.lock();
        try {
            userRepository.updateUser(user.getPhoneNumber(), user.getTgId());
        }finally {
            lock.unlock();
        }
    }
}
