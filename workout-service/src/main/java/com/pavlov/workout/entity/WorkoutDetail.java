package com.pavlov.workout.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Duration;

@Getter
@Entity
public class WorkoutDetail extends AbstractEntity {
    @NotNull
    @Positive
    private long workoutId;

    @Size(max = 63)
    private String name;

    @NotNull
    @Positive
    private int queue;

    private int reps;

    private int sets;

    private Duration exerciseDuration;

    private Duration restDuration;

    @Size(max = 255)
    private String note;
}