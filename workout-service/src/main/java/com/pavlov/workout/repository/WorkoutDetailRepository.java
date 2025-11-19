package com.pavlov.workout.repository;

import com.pavlov.workout.entity.WorkoutDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutDetailRepository extends JpaRepository<WorkoutDetail, Long> {

    void deleteByWorkoutId(Long workoutId);
}
