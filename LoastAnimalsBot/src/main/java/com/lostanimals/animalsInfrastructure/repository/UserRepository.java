package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByTgId(String tgId);
}
