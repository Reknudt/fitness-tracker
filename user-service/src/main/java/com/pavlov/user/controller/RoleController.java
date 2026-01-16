package com.pavlov.user.controller;

import com.pavlov.user.entity.Role;
import com.pavlov.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> all() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    public Role findOne(@PathVariable long id) {
        return roleService.findById(id);
    }
}
