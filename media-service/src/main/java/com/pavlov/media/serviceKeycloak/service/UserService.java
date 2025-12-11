package com.pavlov.media.serviceKeycloak.service;

import com.pavlov.media.serviceKeycloak.request.UserRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j

@AllArgsConstructor
public class UserService {

    private final UsersResource usersResource;
    private final RolesResource rolesResource;

    public List<UserRequest> getAllUsers() {
        try {
            List<UserRequest> userRequests = new ArrayList<>(List.of());
            List<UserRepresentation> userRepresentations = usersResource.list();

            for (UserRepresentation userRepresentation : userRepresentations) {
                UserRequest user = new UserRequest();
                user.setId(userRepresentation.getId());
                user.setUsername(userRepresentation.getUsername());
                user.setEmail(userRepresentation.getEmail());
                user.setEnabled(userRepresentation.isEnabled());
                if (userRepresentation.getAttributes() != null)
                    user.setAttributes(userRepresentation.getAttributes());
                if (userRepresentation.getRequiredActions() != null)
                    user.setRequiredActions(userRepresentation.getRequiredActions());

                user.setRoles(getUserRoles(userRepresentation.getId()));    // remove ?

                userRequests.add(user);
            }
            return userRequests;
        } catch (NotFoundException e) {
            log.warn("User realm found");
            throw new ResponseStatusException(NOT_FOUND, "Realm not found");
        }
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

    public UserRepresentation getUserByUsername(String username) {      //todo return UserRequest
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
            if (Response.Status.CREATED.getStatusCode() != response.getStatus())
                throw new ResponseStatusException(CONFLICT, "User " + newUser.getUsername() + " already exists ?");
            String location = response.getLocation().getPath();
            return location.substring(location.lastIndexOf('/') + 1); // returns userId from uri
        }
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
//            UserResource userResource = usersResource.get(userId);
//            return userResource.roles().clientLevel("8fba07f6-6a2c-4b8e-a2fb-4e0dfdbd790a").listAll();
            return usersResource.get(userId).roles().realmLevel().listAll();    // /admin/realms/{realm}/groups/{group-id}/role-mappings/realm
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

    private static UserRepresentation getUserRepresentation(UserRequest request) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setEnabled(request.isEnabled());
        if (request.getAttributes() != null)
            newUser.setAttributes(request.getAttributes());
        if (request.getRequiredActions() != null)
            newUser.setRequiredActions(request.getRequiredActions());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getUsername());
        credential.setTemporary(true);

        newUser.setCredentials(List.of(credential));

        return newUser;
    }

    // create default user
    /*public void createDefaultUser() {
        UserRepresentation defaultUser = new UserRepresentation();
        defaultUser.setUsername(defaultUsername);
        System.out.println("defaultUserName: " + defaultUser.getUsername());
        System.out.println("DEF USERNAME: " + defaultUsername);
        defaultUser.setEmail(defaultEmail);
        defaultUser.setEnabled(true);
        defaultUser.setFirstName(defaultFirstName);
        defaultUser.setLastName(defaultLastName);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("root");
        credential.setTemporary(false);

        defaultUser.setCredentials(List.of(credential));

        usersResource.create(defaultUser);
/*        Response response = usersResource.create(defaultUser);
        String location = response.getLocation().getPath();
        String userId = location.substring(location.lastIndexOf('/') + 1);

        List<RoleRepresentation> roles = defaultUserRoles.stream()
                .map(roleName -> {
                    try {
                        return rolesResource.get(roleName).toRepresentation();
                    } catch (NotFoundException e) {
                        log.warn("Role '{}' not found for default user", roleName);
                        return null;
                    }
                })
                .filter(Objects::nonNull).toList();

        usersResource.get(userId).roles().realmLevel().add(roles);*/
    //}

}
