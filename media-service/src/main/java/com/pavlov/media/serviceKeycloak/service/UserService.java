package com.pavlov.media.serviceKeycloak.service;

import com.pavlov.media.serviceKeycloak.request.UserRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UsersResource usersResource;
    private final RolesResource rolesResource;

    public List<UserRepresentation> getAllUsers() {
        return usersResource.list();
    }

    public UserRepresentation getUserById(String userId) {
        try {
            UserResource userResource = usersResource.get(userId);
            return userResource.toRepresentation();
        } catch (NotFoundException e) {
            log.warn("User not found by ID: {}", userId);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public UserRepresentation getUserByUsername(String username) {
        try {
            List<UserRepresentation> users = usersResource.search(username, true);
            return users.getFirst();
        } catch (NotFoundException e) {
            log.warn("User not found by username: {}", username);
            throw new ResponseStatusException(NOT_FOUND, "User " + username + " not found");
        }
    }

    public String createUser(UserRequest request) {
        UserRepresentation newUser = getUserRepresentation(request);
        try (Response response = usersResource.create(newUser)) {
            String location = response.getLocation().getPath();     // Получаем ID созданного пользователя из Location header
            return location.substring(location.lastIndexOf('/') + 1);
        }
    }

    @NotNull
    private static UserRepresentation getUserRepresentation(UserRequest request) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setEnabled(request.isEnabled());
        if (request.getAttributes() != null)
            newUser.setAttributes(request.getAttributes());
        if (request.getRequiredActions() != null)
            newUser.setRequiredActions(request.getRequiredActions());
        return newUser;
    }

    public void updateUser(String userId, UserRequest request) {
        try {
            UserResource userResource = usersResource.get(userId);
            UserRepresentation user = userResource.toRepresentation();
            if (request.getEmail() != null)
                user.setEmail(request.getEmail());
            if (request.getAttributes() != null)
                user.setAttributes(request.getAttributes());
            userResource.update(user);
        } catch (NotFoundException e) {
            log.warn("User not found for update: {}", userId);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public void setUserEnabled(String userId, boolean enabled) {
        try {
            UserResource userResource = usersResource.get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(enabled);
            userResource.update(user);
        } catch (NotFoundException e) {
            log.warn("User not found for enable/disable: {}", userId);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public void assignRoleToUser(String userId, String roleName) {
        try {
            UserResource userResource = usersResource.get(userId);
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
            userResource.roles().realmLevel().add(List.of(role));
        } catch (NotFoundException e) {
            log.warn("User or role not found for assignment. User: {}, Role: {}", userId, roleName);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public void removeRoleFromUser(String userId, String roleName) {
        try {
            UserResource userResource = usersResource.get(userId);
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
            userResource.roles().realmLevel().remove(List.of(role));
        } catch (NotFoundException e) {
            log.warn("User not found for unassignment. User: {}, Role: {}", userId, roleName);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public List<RoleRepresentation> getUserRoles(String userId) {
        try {
            UserResource userResource = usersResource.get(userId);
            return userResource.roles().realmLevel().listAll();
        } catch (NotFoundException e) {
            log.warn("User not found for getting roles: {}", userId);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }

    public void deleteUser(String userId) {
        try {
            UserResource userResource = usersResource.get(userId);
            userResource.remove();
        } catch (NotFoundException e) {
            log.warn("User not found for deletion: {}", userId);
            throw new ResponseStatusException(NOT_FOUND, "User " + userId + " not found");
        }
    }
}
