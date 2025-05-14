package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LostAnimalsRepository extends CrudRepository< LostAnimals,Integer> {

    List<LostAnimals> findByStatus(StatusType status);
    List<LostAnimals> findByUser(User user);
}
