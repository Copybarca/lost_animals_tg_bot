package com.lostanimals.animalsInfrastructure.service;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import com.lostanimals.animalsInfrastructure.repository.LostAnimalsRepository;
import com.lostanimals.animalsInfrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    public List<LostAnimals> getAllLostByStatusPartly(StatusType status, PageRequest pageRequest){
        return (List<LostAnimals>) lostAnimalsRepository.findByStatus(status,pageRequest);
    }
    public List<LostAnimals> getAllByUser(User user){
        return (List<LostAnimals>) lostAnimalsRepository.findByUser(user);
    }
    public void deleteLostAnimalsByUser(User user){
        lostAnimalsRepository.deleteAllByUser(user);
    }
    @Transactional
    public void addAnimalForUser(User user, LostAnimals newAnimal) {
        User userInDb = userRepository.findByTgId(user.getTgId());
        newAnimal.setUser(userInDb);
        lostAnimalsRepository.save(newAnimal);
    }
}
