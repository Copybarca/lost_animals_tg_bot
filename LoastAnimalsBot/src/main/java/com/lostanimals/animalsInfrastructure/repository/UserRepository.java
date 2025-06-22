package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByTgId(String tgId);

    @Modifying
    @Query("UPDATE User u SET u.phoneNumber = :phoneNumber WHERE u.tgId = :tgId")
    void updateUser(@Param("phoneNumber") String phoneNumber, @Param("tgId") String tgId);
}
