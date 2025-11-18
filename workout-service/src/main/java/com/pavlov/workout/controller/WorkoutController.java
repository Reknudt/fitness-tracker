package com.pavlov.workout.controller;

import com.pavlov.workout.entity.Workout;
import com.pavlov.workout.model.ActivityType;
import com.pavlov.workout.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
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
    public Page<Workout> getAllWorkouts(@PositiveOrZero @RequestParam(defaultValue = "0") int offset,
                                        @Min(1) @Max(100) @RequestParam(defaultValue = "50") int limit,
                                        @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                                        @RequestParam(defaultValue = "planedAt") String sortField,
                                        @RequestParam Optional<ActivityType> activityType,
                                        @RequestParam Optional<LocalDateTime> planedAt,
                                        @RequestParam Optional<Duration> duration,
                                        @RequestParam Optional<Long> userId) {
        return workoutService.getSlice(PageRequest.of(offset, limit, Sort.by(direction, sortField)),
                activityType.orElse(null), planedAt.orElse(null), duration.orElse(null), userId.orElse(null));
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

    @PatchMapping("/{id}")
    @Operation(summary = "Update Workout isCompleted status")
    public void completeOrOpenWorkout(@PathVariable long id, @RequestParam boolean isCompleted) {
        if (workoutService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "Workout " + id + " not found");
        workoutService.completeOrOpenWorkout(id, isCompleted);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Workout", description = "To set duration use \"PT1H30M5S\" for 1 hour 30 minutes 5 secs")
    @ResponseStatus(NO_CONTENT)
    public void deleteWorkout(@PathVariable long id) {
        if (workoutService.notExist(id))
            throw new ResponseStatusException(NOT_FOUND, "Workout " + id + " not found");
        workoutService.deleteWorkout(id);
    }
}
