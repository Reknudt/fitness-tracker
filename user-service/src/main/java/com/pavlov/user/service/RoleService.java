package com.pavlov.user.service;

import com.pavlov.user.entity.Role;
import com.pavlov.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(long id) {
        return roleRepository.findById(id).orElseThrow();
    }
}
