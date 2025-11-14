package com.pavlov.workout.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Entity
public class Workout extends AbstractEntity {

    @NotNull
    @Positive
    private long userId;

    @NotBlank
    @Size(max = 63)
    private String name;

    @PositiveOrZero
    @Digits(integer = 5, fraction = 2)
    private BigDecimal calories;

//    @NotNull
//    @Enumerated(EnumType.ORDINAL)
//    private ActivityType activityType;    //todo add enums

//    @NotNull
//    @Enumerated(EnumType.ORDINAL)
//    private PlanType planType;

    private LocalDateTime planedAt;

    private Duration duration;

    @Size(max = 255)
    private String note;

    @NotNull
    private boolean isCompleted;
}
