package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimal;
import com.lostanimals.animalsInfrastructure.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LostAnimalsRepository extends CrudRepository<LostAnimal,Integer> {

    @Query("FROM LostAnimal l WHERE l.status = :status")
    @Transactional
    List<LostAnimal> findByStatus(@Param("status") StatusType status, PageRequest pageRequest);
    @Query("FROM LostAnimal l WHERE l.user = :user")
    @Transactional
    List<LostAnimal> findByUser(User user);


    void deleteAllByUser(User user);
}
