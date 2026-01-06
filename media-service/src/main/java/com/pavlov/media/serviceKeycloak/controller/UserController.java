package com.pavlov.media.serviceKeycloak.controller;

import com.pavlov.media.serviceKeycloak.request.UserRequest;
import com.pavlov.media.serviceKeycloak.service.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/auth/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserRequest> findAll() {
        return userService.getAllUsers();
    }

//    @GetMapping("/{username}")
//    public UserRepresentation findById(@PathVariable String name) {
//        return userService.getUserByUsername(name);
//    }

    @GetMapping("/{id}")
    public UserRepresentation findById(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/roles")
    public List<RoleRepresentation> findRolesById(@PathVariable("id") String id) {
        return userService.getUserRoles(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Response createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/roles")
    public void assignRolesFromUser(@PathVariable("id") String id, @RequestBody List<String> roleNames) {
        userService.assignRolesToUser(id, roleNames);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable("id") String id, @RequestBody UserRequest userRequest) {
        if (!Objects.equals(id, userRequest.getId()))
            throw new ResponseStatusException(BAD_REQUEST, "IDs are not equal in request url and body");
        userService.updateUser(userRequest);
    }

    @PatchMapping("/{id}")
    public void enableUser(@PathVariable("id") String id, @RequestBody boolean isEnabled) {
        userService.setUserEnabled(id, isEnabled);
    }

    @PatchMapping("/{id}/credentials")
    public void setCredentials(@PathVariable("id") String id, @RequestBody String password) {
        userService.setCredentials(id, password);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Response deleteUser(@PathVariable("id") String id) {
        return userService.deleteUser(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/roles")
    public void removeRolesFromUser(@PathVariable("id") String id, @RequestBody List<String> roleNames) {
        userService.removeRolesFromUser(id, roleNames);
    }
}
