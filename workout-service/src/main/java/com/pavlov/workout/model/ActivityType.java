package com.pavlov.workout.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
public enum ActivityType { // Order is important because in DB starts with 1, fake NONE.ordinal = 0
    NONE("N/A"),
    AEROBIC("Аэробная"),
    ANAEROBIC("Анаэробная"),
    HYBRID("Гибрид");

    private final String value;

//    public static final Map<Integer, String> NAMES = Arrays.stream(values()).filter(e -> 0 != e.ordinal()).collect(toUnmodifiableMap(Enum::ordinal, e -> e.value));

    ActivityType(String value) {
        this.value = value;
    }

}
