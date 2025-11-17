package com.pavlov.workout.controller;

import com.pavlov.workout.entity.Workout;
import com.pavlov.workout.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    @Operation(summary = "Get all Workouts")
    public List<Workout> getAllWorkouts(@RequestParam Optional<Long> userId) {
        if (userId.isPresent())
            return workoutService.findByUserId(userId.get());
        return workoutService.findAllWorkouts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Workout by 'id'")
    public Optional<Workout> getWorkout(@PathVariable Long id) {
        return workoutService.findOne(id);
    }

    @PostMapping
    @Operation(summary = "Create Workout", description = "To set duration use \"PT1H30M5S\" for 1 hour 30 minutes 5 secs")
    @ResponseStatus(CREATED)
    public Workout createWorkout(@Valid @RequestBody Workout workout) {
        return workoutService.saveWorkout(workout);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Workout")
    public void updateWorkout(@PathVariable long id, @Valid @RequestBody Workout workout) {
        if (!Objects.equals(id, workout.getId()))
            throw new ResponseStatusException(BAD_REQUEST, "IDs are not equal in request url and body");
        if (workoutService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "Workout " + id + " not found");
        workoutService.saveWorkout(workout);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Update Workout", description = "To set duration use \"PT1H30M5S\" for 1 hour 30 minutes 5 secs")
    @ResponseStatus(NO_CONTENT)
    public void deleteWorkout(@PathVariable long id) {
        if (workoutService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "Workout " + id + " not found");
        workoutService.deleteWorkout(id);
    }
}
