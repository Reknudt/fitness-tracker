package com.pavlov.workout.controller;

import com.pavlov.workout.entity.WorkoutDetail;
import com.pavlov.workout.service.WorkoutDetailService;
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
@RequestMapping(value = "/api/v1/workout-details")
public class WorkoutDetailController {

    private final WorkoutDetailService workoutDetailService;

    @GetMapping
    @Operation(summary = "Get all WorkoutDetails")
    public List<WorkoutDetail> getAllWorkouts(@RequestParam Long workoutId) {
        return workoutDetailService.findAllByWorkoutId(workoutId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get WorkoutDetail by 'id'")
    public Optional<WorkoutDetail> getWorkout(@PathVariable Long id) {
        return workoutDetailService.findOne(id);
    }

    @PostMapping
    @Operation(summary = "Create WorkoutDetail", description = "To set duration use \"PT1H30M5S\" for 1 hour 30 minutes 5 secs")
    @ResponseStatus(CREATED)
    public WorkoutDetail createWorkoutDetail(@Valid @RequestBody WorkoutDetail workoutDetail) {
        return workoutDetailService.saveWorkoutDetail(workoutDetail);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update WorkoutDetail", description = "To set duration use \"PT1H30M5S\" for 1 hour 30 minutes 5 secs")
    public void updateWorkoutDetail(@PathVariable long id, @Valid @RequestBody WorkoutDetail workout) {
        if (!Objects.equals(id, workout.getId()))
            throw new ResponseStatusException(BAD_REQUEST, "IDs are not equal in request url and body");
        if (workoutDetailService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "WorkoutDetail " + id + " not found");
        workoutDetailService.saveWorkoutDetail(workout);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Update WorkoutDetail")
    @ResponseStatus(NO_CONTENT)
    public void deleteWorkoutDetail(@PathVariable long id) {
        if (workoutDetailService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "WorkoutDetail " + id + " not found");
        workoutDetailService.deleteWorkoutDetail(id);
    }
}
