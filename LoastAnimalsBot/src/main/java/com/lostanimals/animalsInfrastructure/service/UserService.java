package com.lostanimals.animalsInfrastructure.service;

import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.repository.LostAnimalsRepository;
import com.lostanimals.animalsInfrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional(readOnly = true)
    public List<LostAnimals> getLostAnimals(String tgId) {
        User user = userRepository.findByTgId(tgId);
        return user.getLostAnimals();
    }
    public User getUserByTgID(String tgId) {
        return userRepository.findByTgId(tgId);
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
