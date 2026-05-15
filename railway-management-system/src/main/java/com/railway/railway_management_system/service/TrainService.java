package com.railway.railway_management_system.service;

import com.railway.railway_management_system.enums.TrainType;
import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.SeatConfig;
import com.railway.railway_management_system.model.Train;
import com.railway.railway_management_system.repository.TrainRepository;
import com.railway.railway_management_system.requestDto.AddTrainRequest;
import com.railway.railway_management_system.requestDto.UpdateTrainRequest;
import com.railway.railway_management_system.responseDto.SeatConfigDto;
import com.railway.railway_management_system.responseDto.TrainDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainService implements ITrainService {

    private final TrainRepository trainRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Train> getAllTrains() {
        List<Train> trains = trainRepository.findAll();
        if (trains.isEmpty()) {
            throw new ResourceNotFoundException("No trains found!");
        }
        return trains;
    }

    @Override
    public TrainDto convertToDto(Train train) {
        return modelMapper.map(train, TrainDto.class);
    }

    @Override
    public Train getTrainById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found!"));
    }

    @Override
    public Train getTrainByTrainNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found!"));
    }

    @Override
    public List<Train> getTrainByName(String trainName) {
        List<Train> trains = trainRepository.findByName(trainName);
        if (trains.isEmpty()) {
            throw new ResourceNotFoundException("No trains found with name: " + trainName);
        }
        return trains;
    }

    @Override
    public List<Train> getTrainByType(String type) {
        List<Train> trains = trainRepository.findByType(TrainType.valueOf(type));
        if (trains.isEmpty()) {
            throw new ResourceNotFoundException("No trains found with type: " + type);
        }
        return trains;
    }

    @Override
    public Train addTrain(AddTrainRequest request) {
        Train train = new Train();
        train.setName(request.getName());
        train.setType(TrainType.valueOf(request.getType()));
        mapSeatConfigurations(train, request.getSeatConfigurations());
        return trainRepository.save(train);
    }

    @Override
    @Transactional
    public Train updateTrain(UpdateTrainRequest updateRequest, Long id) {
        Optional<Train> trainOptional = trainRepository.findById(id);
        if (trainOptional.isPresent()) {
            Train train = trainOptional.get();
            train.setName(updateRequest.getName());
            train.setType(TrainType.valueOf(updateRequest.getType()));
            mapSeatConfigurations(train, updateRequest.getSeatConfigurations());
            return trainRepository.save(train);
        } else {
            throw new ResourceNotFoundException("Train not found!");
        }
    }

    @Override
    @Transactional
    public Train updateTrainByTrainNumber(UpdateTrainRequest updateRequest, String trainNumber) {
        Optional<Train> trainOptional = trainRepository.findByTrainNumber(trainNumber);
        if (trainOptional.isPresent()) {
            Train train = trainOptional.get();
            train.setName(updateRequest.getName());
            train.setType(TrainType.valueOf(updateRequest.getType()));
            mapSeatConfigurations(train, updateRequest.getSeatConfigurations());
            return trainRepository.save(train);
        } else {
            throw new ResourceNotFoundException("Train not found!");
        }
    }

    @Override
    public void deleteTrainById(Long id) {
        trainRepository.findById(id)
                .ifPresentOrElse(trainRepository::delete,
                        () -> { throw new ResourceNotFoundException("Train not found!"); });
    }

    @Override
    public void deleteTrainByTrainNumber(String trainNumber) {
        trainRepository.findByTrainNumber(trainNumber)
                .ifPresentOrElse(trainRepository::delete,
                        () -> { throw new ResourceNotFoundException("Train not found!"); });
    }

    private void mapSeatConfigurations(Train train, List<SeatConfigDto> seatConfigDtos) {
        if (seatConfigDtos == null) {
            return;
        }
        List<SeatConfig> seatConfigs = seatConfigDtos.stream()
                .map(dto -> {
                    SeatConfig seatConfig = modelMapper.map(dto, SeatConfig.class);
                    seatConfig.setTrain(train);
                    return seatConfig;
                })
                .collect(Collectors.toList());
        if (train.getSeatConfigurations() == null) {
            // New entity — safe to set directly
            train.setSeatConfigurations(seatConfigs);
        } else {
            // Managed entity — must mutate the Hibernate-tracked collection in-place;
            // replacing it with setSeatConfigurations(newList) breaks orphanRemoval tracking.
            train.getSeatConfigurations().clear();
            train.getSeatConfigurations().addAll(seatConfigs);
        }
    }
}

