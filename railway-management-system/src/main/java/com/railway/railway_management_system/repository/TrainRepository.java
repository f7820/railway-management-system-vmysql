package com.railway.railway_management_system.repository;

import com.railway.railway_management_system.enums.TrainType;
import com.railway.railway_management_system.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    List<Train> findByName(String name);

    List<Train> findByType(TrainType type);
}

