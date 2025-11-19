package com.pavlov.workout.service;

import com.pavlov.workout.entity.WorkoutDetail;
import com.pavlov.workout.repository.WorkoutDetailRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WorkoutDetailService {

    private final WorkoutDetailRepository workoutDetailRepository;

    public List<WorkoutDetail> findAllByWorkoutId(Long workoutId) {
        return workoutDetailRepository.findAll();
    }

    public Optional<WorkoutDetail> findOne(Long id) {
        return workoutDetailRepository.findById(id);
    }

    public boolean notExist(long currencyRateId) {
        return !workoutDetailRepository.existsById(currencyRateId);
    }

//    @Transactional
    public WorkoutDetail saveWorkoutDetail(WorkoutDetail workoutDetail) {
        return workoutDetailRepository.save(workoutDetail);
    }

    @Transactional
    public void deleteWorkoutDetail(Long id) {
        if (!workoutDetailRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkoutDetail " + id + " not found");
        workoutDetailRepository.deleteById(id);
    }
}
