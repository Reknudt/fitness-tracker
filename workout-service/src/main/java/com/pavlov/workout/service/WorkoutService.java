package com.pavlov.workout.service;

import com.pavlov.workout.entity.Workout;
import com.pavlov.workout.model.ActivityType;
import com.pavlov.workout.repository.WorkoutDetailRepository;
import com.pavlov.workout.repository.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutDetailRepository workoutDetailRepository;

    public Page<Workout> getSlice(Pageable pageable, ActivityType activityType, LocalDateTime planedAt, Duration duration, Long userId) {
        return workoutRepository.findAll(pageable, activityType, planedAt, duration, userId);
    }

    public Optional<Workout> findOne(Long id) {
        return workoutRepository.findById(id);
    }

    public boolean notExist(long currencyRateId) {
        return !workoutRepository.existsById(currencyRateId);
    }

//    @Transactional
    public Workout saveWorkout(Workout workout) {
        return workoutRepository.save(workout);
    }

//    @Transactional
    public void completeOrOpenWorkout(long id, boolean isCompleted) {
        workoutRepository.updateIsCompleted(id, isCompleted);
    }

    @Transactional
    public void deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout " + id + " not found");
        workoutDetailRepository.deleteByWorkoutId(id);
        workoutRepository.deleteById(id);
    }
}
