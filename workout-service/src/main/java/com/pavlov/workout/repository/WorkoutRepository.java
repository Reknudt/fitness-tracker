package com.pavlov.workout.repository;

import com.pavlov.workout.entity.Workout;
import com.pavlov.workout.model.ActivityType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("""
        SELECT w FROM Workout w
            WHERE w.activityType = coalesce(:activityType, w.activityType)
                AND w.planedAt = coalesce(:planedAt, w.planedAt)
                AND w.duration = coalesce(:duration, w.duration)
                AND w.userId = coalesce(:userId, w.userId)""")
    Page<Workout> findAll(Pageable pageable, ActivityType activityType, LocalDateTime planedAt, Duration duration, Long userId);

    List<Workout> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Workout w SET w.isCompleted = :isCompleted WHERE w.id = :id")
    void updateIsCompleted(long id, boolean isCompleted);

}
