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
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LostAnimalsService {
    private final LostAnimalsRepository lostAnimalsRepository;
    private final UserRepository userRepository;
    @Autowired
    public LostAnimalsService(LostAnimalsRepository lostAnimalsRepository, UserRepository userRepository) {
        this.lostAnimalsRepository = lostAnimalsRepository;
        this.userRepository = userRepository;
    }
    private static ReentrantLock lock = new ReentrantLock();

    public  List<LostAnimal> getAllLostByStatusPartly(StatusType status, PageRequest pageRequest){
        return (List<LostAnimal>) lostAnimalsRepository.findByStatus(status,pageRequest);
    }
    @Transactional
    public List<LostAnimal> getAllByUser(User user){
        return (List<LostAnimal>) lostAnimalsRepository.findByUser(user);
    }
    @Transactional
    public void deleteAllLostAnimalsByUser(User user){
        lock.lock();
        try {
            lostAnimalsRepository.deleteAllByUser(user);
        }finally {
            lock.unlock();
        }

    }
    @Transactional
    public void addAnimalForUser(User user, LostAnimal newAnimal) {
        lock.lock();
        try{
            User userInDb = userRepository.findByTgId(user.getTgId());
            if (userInDb == null) {
                throw new IllegalArgumentException("Пользователь не найден");
            }
            newAnimal.setUser(userInDb);
            lostAnimalsRepository.save(newAnimal);
        }finally {
            lock.unlock();
        }

    }
}
