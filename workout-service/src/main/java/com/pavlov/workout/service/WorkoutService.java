package com.pavlov.workout.service;

import com.pavlov.workout.entity.Workout;
import com.pavlov.workout.repository.WorkoutDetailRepository;
import com.pavlov.workout.repository.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutDetailRepository workoutDetailRepository;

    public List<Workout> findAllWorkouts() {
        return workoutRepository.findAll();
    }

    public Optional<Workout> findOne(Long id) {
        return workoutRepository.findById(id);
    }

    public List<Workout> findByUserId(Long userId) {
        return workoutRepository.findByUserId(userId);
    }

    public boolean notExist(long currencyRateId) {
        return !workoutRepository.existsById(currencyRateId);
    }

//    @Transactional
    public Workout saveWorkout(Workout workout) {
        return workoutRepository.save(workout);
    }

    @Transactional
    public void deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout " + id + " not found");
        workoutDetailRepository.deleteByWorkoutId(id);
        workoutRepository.deleteById(id);
    }
}
