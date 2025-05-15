package com.lostanimals.animalsInfrastructure.service;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.repository.LostAnimalsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LostAnimalsService {
    private final LostAnimalsRepository lostAnimalsRepository;
    @Autowired
    public LostAnimalsService(LostAnimalsRepository lostAnimalsRepository) {
        this.lostAnimalsRepository = lostAnimalsRepository;
    }

    public List<LostAnimals> getAllLostByStatusPartly(StatusType status, PageRequest pageRequest){
        return (List<LostAnimals>) lostAnimalsRepository.findByStatus(status,pageRequest);
    }
}
