package com.lostanimals.animalsInfrastructure.service;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimal;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.repository.LostAnimalsRepository;
import com.lostanimals.animalsInfrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LostAnimalsService {
    private final LostAnimalsRepository lostAnimalsRepository;
    private final UserRepository userRepository;
    @Autowired
    public LostAnimalsService(LostAnimalsRepository lostAnimalsRepository, UserRepository userRepository) {
        this.lostAnimalsRepository = lostAnimalsRepository;
        this.userRepository = userRepository;
    }
    public List<LostAnimal> getAllLostByStatusPartly(StatusType status, PageRequest pageRequest){
        return (List<LostAnimal>) lostAnimalsRepository.findByStatus(status,pageRequest);
    }
    public List<LostAnimal> getAllByUser(User user){
        return (List<LostAnimal>) lostAnimalsRepository.findByUser(user);
    }
    public void deleteLostAnimalsByUser(User user){
        lostAnimalsRepository.deleteAllByUser(user);
    }
    @Transactional
    public void addAnimalForUser(User user, LostAnimal newAnimal) {
        User userInDb = userRepository.findByTgId(user.getTgId());
        newAnimal.setUser(userInDb);
        lostAnimalsRepository.save(newAnimal);
    }
}
