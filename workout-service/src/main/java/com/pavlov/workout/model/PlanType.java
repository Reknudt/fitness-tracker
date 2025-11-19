package com.pavlov.workout.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
public enum PlanType { // Order is important because in DB starts with 1, fake NONE.ordinal = 0
    NONE("N/A"),
    LINEAR("Линейная"),
    CIRCLE("Круговая");

    private final String value;

//    public static final Map<Integer, String> NAMES = Arrays.stream(values()).filter(e -> 0 != e.ordinal()).collect(toUnmodifiableMap(Enum::ordinal, e -> e.value));

    PlanType(String value) {
        this.value = value;
    }

}
