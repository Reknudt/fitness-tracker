package com.pavlov.user.controller;

import com.pavlov.user.entity.Consumer;
import com.pavlov.user.entity.Role;
import com.pavlov.user.service.ConsumerService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/consumers")
public class ConsumerController {

    private final ConsumerService consumerService;

    @GetMapping
    @Operation(summary = "Get all Consumers")
    public Page<Consumer> getAllConsumers(@PositiveOrZero @RequestParam(defaultValue = "0") int offset,
                                      @Min(1) @Max(100) @RequestParam(defaultValue = "50") int limit,
                                      @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                                      @RequestParam(defaultValue = "username") String sortField,
                                      @RequestParam Optional<LocalDate> birthDate,
                                      @RequestParam Optional<BigDecimal> weight) {
        return consumerService.getSlice(PageRequest.of(offset, limit, Sort.by(direction, sortField)),
                birthDate.orElse(null), weight.orElse(null));
    }

    @GetMapping("/{id}")
    public Consumer findOne(@PathVariable long id) {
        return consumerService.findById(id);
    }

    @GetMapping("/{id}/roles")
    public List<Role> findRolesByConsumerId(@PathVariable long id) {
        return consumerService.findRolesByConsumerId(id);
    }

    @PostMapping
    public Consumer create(@Valid @RequestBody Consumer consumer) {
        return consumerService.createConsumer(consumer);
    }

    @PostMapping("/{id}/roles")
    public void assignRoles(@PathVariable long id, @RequestParam List<Long> roleIds) {
        consumerService.assignRolesToConsumer(id, roleIds);
    }

    @PutMapping("/{id}")
    public Consumer update(@PathVariable long id, @Valid @RequestBody Consumer consumer) {
        return consumerService.updateConsumer(id, consumer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        consumerService.deleteConsumerById(id);
    }

    @DeleteMapping("/{id}/roles")
    public void removeRoles(@PathVariable long id, @RequestParam List<Long> roleIds) {
        consumerService.removeRolesFromConsumer(id, roleIds);
    }

}
