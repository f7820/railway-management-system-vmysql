package com.railway.railway_management_system.service;

import com.railway.railway_management_system.model.Train;
import com.railway.railway_management_system.requestDto.AddTrainRequest;
import com.railway.railway_management_system.requestDto.UpdateTrainRequest;
import com.railway.railway_management_system.responseDto.TrainDto;

import java.util.List;

public interface ITrainService {
    List<Train> getAllTrains();
    TrainDto convertToDto(Train train);
    Train getTrainById(Long id);
    Train getTrainByTrainNumber(String trainNumber);
    List<Train> getTrainByName(String trainName);
    List<Train> getTrainByType(String type);
    Train addTrain(AddTrainRequest request);
    Train updateTrain(UpdateTrainRequest updateRequest, Long id);
    Train updateTrainByTrainNumber(UpdateTrainRequest updateRequest, String trainNumber);
    void deleteTrainById(Long id);
    void deleteTrainByTrainNumber(String trainNumber);
}
