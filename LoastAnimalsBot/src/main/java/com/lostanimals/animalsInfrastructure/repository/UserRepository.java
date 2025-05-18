package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByTgId(String tgId);

}
