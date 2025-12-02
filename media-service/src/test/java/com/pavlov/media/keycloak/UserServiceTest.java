package com.pavlov.media.keycloak;

import com.pavlov.media.serviceKeycloak.request.RoleRequest;
import com.pavlov.media.serviceKeycloak.request.UserRequest;
import com.pavlov.media.serviceKeycloak.service.RoleService;
import com.pavlov.media.serviceKeycloak.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private String generateUniqueUsername(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateUniqueRoleName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Should create user and verify it exists")
    void createUser_WithValidData_ShouldCreateUser() {
        String username = generateUniqueUsername("testuser");
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setAttributes(Map.of("fullname", List.of("fullname test")));
//        request.setFirstName("Test");
//        request.setLastName("User");
        request.setEnabled(true);

        String userId = null;
        try {
            userId = userService.createUser(request);

            assertNotNull(userId);
            UserRepresentation user = userService.getUserById(userId);
            assertNotNull(user);
            assertEquals(username, user.getUsername());
            assertEquals(username + "@example.com", user.getEmail());
            assertTrue(user.isEnabled());
        } finally {
            if (userId != null)
                safeDeleteUser(userId);
        }
    }

    @Test
    @DisplayName("Should get user by username")
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        String username = generateUniqueUsername("getuser");
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setAttributes(Map.of("fullname", List.of("fullname test get")));
//        request.setFirstName("Get");
//        request.setLastName("User");

        String userId = null;
        try {
            userId = userService.createUser(request);
            UserRepresentation user = userService.getUserByUsername(username);

            assertNotNull(user);
            assertEquals(username, user.getUsername());
            assertEquals(username + "@example.com", user.getEmail());
        } finally {
//            if (userId != null)
//                safeDeleteUser(userId);
        }
    }

    @Test
    @DisplayName("Should assign role to user")
    void assignRoleToUser_ShouldAssignRoleSuccessfully() {
        String username = generateUniqueUsername("roleuser");
        String roleName = generateUniqueRoleName("USER_ROLE");
        
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setEmail(username + "@example.com");
        userRequest.setAttributes(Map.of("fullname", List.of("fullname test assign")));
        String userId = userService.createUser(userRequest);

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName(roleName);
        roleRequest.setDescription("Test role for assignment");
        roleService.createRole(roleRequest);

        try {
            userService.assignRoleToUser(userId, roleName);

            List<RoleRepresentation> userRoles = userService.getUserRoles(userId);
            assertFalse(userRoles.isEmpty());
            assertTrue(userRoles.stream().anyMatch(role -> roleName.equals(role.getName())));
        } finally {
            safeDeleteUser(userId);
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should enable and disable user")
    void setUserEnabled_ShouldChangeUserStatus() {
        String username = generateUniqueUsername("statususer");
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setEnabled(true);
        request.setAttributes(Map.of("fullname", List.of("fullname test enable")));

        String userId = userService.createUser(request);

        try {
            // disable user
            userService.setUserEnabled(userId, false);
            UserRepresentation disabledUser = userService.getUserById(userId);
            assertFalse(disabledUser.isEnabled());

            // enable user
            userService.setUserEnabled(userId, true);
            UserRepresentation enabledUser = userService.getUserById(userId);
            assertTrue(enabledUser.isEnabled());
        } finally {
            safeDeleteUser(userId);
        }
    }

    @Test
    @DisplayName("Should update user information")
    void updateUser_ShouldModifyUserData() {
        String username = generateUniqueUsername("updateuser");
        UserRequest createRequest = new UserRequest();
        createRequest.setUsername(username);
        createRequest.setEmail("old@example.com");
//        createRequest.setFirstName("Old");
//        createRequest.setLastName("Name");
        createRequest.setAttributes(Map.of("fullname", List.of("old fullname")));

        String userId = userService.createUser(createRequest);

        try {
            UserRequest updateRequest = new UserRequest();
            updateRequest.setEmail("new@example.com");
//            updateRequest.setFirstName("New");
//            updateRequest.setLastName("Name");
            updateRequest.setAttributes(Map.of("fullname", List.of("new fullname")));

            userService.updateUser(userId, updateRequest);

            UserRepresentation updatedUser = userService.getUserById(userId);
            assertEquals("new@example.com", updatedUser.getEmail());
            assertEquals("new fullname", updatedUser.getAttributes().get("fullname").getFirst());
//            assertEquals("New", updatedUser.getFirstName());
//            assertEquals("Name", updatedUser.getLastName());
        } finally {
            safeDeleteUser(userId);
        }
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        String username = generateUniqueUsername("deleteuser");
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setAttributes(Map.of("fullname", List.of("del fullname")));

        String userId = userService.createUser(request);
        System.out.println("userId: " + userId);
        assertNotNull(userService.getUserById(userId)); // Verify user exists

        userService.deleteUser(userId);

        assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
    }

    // Helper methods
    private void safeDeleteUser(String userId) {
        try {
            userService.deleteUser(userId);
        } catch (Exception e) {
            System.err.println("Cleanup warning: Failed to delete user " + userId + ": " + e.getMessage());
        }
    }

    private void safeDeleteRole(String roleName) {
        try {
            if (roleService.roleExists(roleName))
                roleService.deleteRole(roleName);
        } catch (Exception e) {
            System.err.println("Cleanup warning: Failed to delete role " + roleName + ": " + e.getMessage());
        }
    }
}