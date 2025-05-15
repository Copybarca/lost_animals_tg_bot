package com.lostanimals.animalsInfrastructure.repository;

import com.lostanimals.animalsInfrastructure.appliedAnimalsEnums.StatusType;
import com.lostanimals.animalsInfrastructure.model.LostAnimals;
import com.lostanimals.animalsInfrastructure.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LostAnimalsRepository extends CrudRepository< LostAnimals,Integer> {

    @Query("FROM LostAnimals l WHERE l.status = :status")
    @Transactional
    List<LostAnimals> findByStatus(@Param("status") StatusType status, PageRequest pageRequest);
    List<LostAnimals> findByUser(User user);
}
