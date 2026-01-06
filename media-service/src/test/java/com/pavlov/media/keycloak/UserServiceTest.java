package com.pavlov.media.keycloak;

import com.pavlov.media.serviceKeycloak.request.UserRequest;
import com.pavlov.media.serviceKeycloak.service.RoleService;
import com.pavlov.media.serviceKeycloak.service.UserService;
import jakarta.ws.rs.core.Response;
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

//    @Autowired
//    private UsersResource usersResource;

    private String generateUniqueUsername(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateUniqueRoleName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Should create user and verify it exists")
    void createUser_WithValidData_ShouldCreateUser() {
//        String username = generateUniqueUsername("booker_user2");
        String username = "booker_user1";
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setAttributes(Map.of("fullName", List.of("fullname test 1")));
//        request.setAttributes(Map.of("fullname", List.of("fullname test")));
//        request.setFirstName("Test");
//        request.setLastName("User");
        request.setEnabled(true);

        String userId = null;
        try {
//            userId = userService.createUser(request);
            assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(request).getStatus());
            userId = userService.getUserByUsername(username).getId();

            assertNotNull(userId);
            UserRepresentation user = userService.getUserById(userId);
            assertNotNull(user);
            assertEquals(username, user.getUsername());
            assertEquals(username + "@example.com", user.getEmail());
            assertTrue(user.isEnabled());
        } finally {
//            if (userId != null)
//                safeDeleteUser(userId);
        }
    }

    @Test
    @DisplayName("Simpler user create")
    void createUser() {
        String username = "user5";
        UserRequest request = new UserRequest();
        request.setUsername(username);

        try (Response response = userService.createUser(request)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
        String userId = userService.getUserByUsername(username).getId();
        try (Response response = userService.deleteUser(userId + "123")) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }

        userService.setCredentials(userId, "password");
    }

    @Test
    @DisplayName("Create user with no username")
    void createUserWithNoUsername_shouldReturnBadRequest() {
        UserRequest request = new UserRequest();
        try (Response response = userService.createUser(request)) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @DisplayName("Should get user by username")
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        String username = generateUniqueUsername("getuser");
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setAttributes(Map.of("fullName", List.of("fullname test get")));
//        request.setFirstName("Get");
//        request.setLastName("User");

        String userId = null;
        try {
//            userId = userService.createUser(request);
            assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(request).getStatus());
            userId = userService.getUserByUsername(username).getId();
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
    @DisplayName("Print all users")
    void getAllUsers_ShouldReturnList() {
        List<UserRequest> users = userService.getAllUsers();
        for (UserRequest user : users) {
            System.out.println("userId: " + user.getId() + ", username: " + user.getUsername() + ", email: " + user.getEmail() /*+ ", attributes: " + user.getAttributes().get("fullName") */);
            if (user.getRoles() != null) {
                for (RoleRepresentation role: user.getRoles()) {
                    System.out.println("roles: " + role.getName());
                }
            }
            if (user.getAttributes() != null)
                System.out.println("Attributes: " + user.getAttributes().get("fullName"));
        }
    }

//    @Test
//    @DisplayName("Print all users")
//    void getAllUsers_ShouldReturnList() {
//        List<UserRepresentation> users = userService.getAllUsers();
//        for (UserRepresentation user : users) {
//            System.out.println("userId: " + user.getId() + ", username: " + user.getUsername() + ", email: " + user.getEmail() /*+ ", attributes: " + user.getAttributes().get("fullName") */);
//            if (user.getRealmRoles() != null) {   //roles from UserRepresentation are always null
//                for (String realmRole : user.getRealmRoles()) {
//                    System.out.println("Realm roles: " + realmRole);
//                }
//            }
//            if (user.getClientRoles() != null) {
//                Collection<List<String>> values = user.getClientRoles().values();
//                for (List<String> collection : values) {
//                    for (String s : collection) {
//                        System.out.println("Client role: " + s);
//                    }
//                }
//            }
//            if (user.getAttributes() != null)
//                System.out.println("Attributes: " + user.getAttributes().get("fullName"));
//        }
//    }

    @Test
    @DisplayName("Get user ROLES by service")
    void getUserRoles_ShouldReturnListOfRoleRepresentations() {
        List<RoleRepresentation> roles = userService.getUserRoles("36a5462f-e629-49f6-bc4d-21703ccc40be");
        for (RoleRepresentation role : roles) {
            System.out.println("roles: " + role);
        }
    }

//    @Test
//    @DisplayName("Get user ROLES")
//    void getUserRolesFromResource_ShouldReturnListOfRoleRepresentations() {
////        UserRepresentation userRepresentation = usersResource.get("36a5462f-e629-49f6-bc4d-21703ccc40be").toRepresentation();
//        UserRepresentation userRepresentation = usersResource.search("123", 0, 1, false).getFirst();
//
//        for (String role : userRepresentation.getRealmRoles()) {
//            System.out.println("roles: " + role);
//        }
//    }

    @Test
    @DisplayName("Should assign and remove role to user")
    void assignRoleToUser_ShouldAssignRoleSuccessfully() {
//        String username = generateUniqueUsername("roleuser");
//        String roleName = generateUniqueRoleName("USER_ROLE");
        String username = "booker_user1";
        String roleName = "booker";

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
//        userRequest.setEmail(username + "@example.com");
//        userRequest.setAttributes(Map.of("fullname", List.of("fullname test assign")));
        /*String userId = */userService.createUser(userRequest);

//        RoleRequest roleRequest = new RoleRequest();
//        roleRequest.setName(roleName);
//        roleRequest.setDescription("Test role for assignment");
//        roleService.createRole(roleRequest);

        try {
            String userId = userService.getUserByUsername(username).getId();
            userService.assignRolesToUser(userId, List.of(roleName));

            List<RoleRepresentation> userRoles = userService.getUserRoles(userId);
            assertFalse(userRoles.isEmpty());
            assertTrue(userRoles.stream().anyMatch(role -> roleName.equals(role.getName())));

            userService.assignRolesToUser(userId, List.of("admin", "car_view"));


//            userService.removeRoleFromUser(userId, roleName);

//            userRoles = userService.getUserRoles(userId);
//            assertFalse(userRoles.stream().anyMatch(role -> roleName.equals(role.getName())));
        } finally {
//            safeDeleteUser(userId);
//            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should assign roles to user")
    void assignRolesToUser_ShoulfAssignRoleSuccessfully() {
        //        String username = generateUniqueUsername("roleuser");
//        String roleName = generateUniqueRoleName("USER_ROLE");
        String username = "test_user3";

        List<String> roleNames = List.of("account_edit", "document_process", "viewer");

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(username);
        userRequest.setEmail(username + "@example.com");
        userRequest.setAttributes(Map.of("fullname", List.of("fullname test assign")));
//        String userId = userService.createUser(userRequest);
        assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(userRequest).getStatus());
        String userId = userService.getUserByUsername(username).getId();

//        RoleRequest roleRequest = new RoleRequest();
//        roleRequest.setName(roleName);
//        roleRequest.setDescription("Test role for assignment");
//        roleService.createRole(roleRequest);

        try {
//            String userId = userService.getUserByUsername(username).getId();
            userService.assignRolesToUser(userId, roleNames);

            List<RoleRepresentation> userRoles = userService.getUserRoles(userId);
            assertFalse(userRoles.isEmpty());
            assertTrue(userRoles.stream().anyMatch(role -> roleNames.getFirst().equals(role.getName())));

            userService.removeRolesFromUser(userId, roleNames);

            userRoles = userService.getUserRoles(userId);
            assertFalse(userRoles.stream().anyMatch(role -> roleNames.getFirst().equals(role.getName())));
        } finally {
//            safeDeleteUser(userId);
//            for (String roleName: roleNames) {
//                safeDeleteRole(roleName);
//            }
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

//           String userId = userService.createUser(request);
        assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(request).getStatus());
        String userId = userService.getUserByUsername(username).getId();

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

//        String userId = userService.createUser(createRequest);
//           String userId = userService.createUser(request);
        assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(createRequest).getStatus());
        String userId = userService.getUserByUsername(username).getId();

        try {
            UserRequest updateRequest = new UserRequest();
            updateRequest.setEmail("new@example.com");
//            updateRequest.setFirstName("New");
//            updateRequest.setLastName("Name");
            updateRequest.setAttributes(Map.of("fullname", List.of("new fullname")));

            userService.updateUser(updateRequest);

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

        //           String userId = userService.createUser(request);
        assertEquals(Response.Status.CREATED.getStatusCode(), userService.createUser(request).getStatus());
        String userId = userService.getUserByUsername(username).getId();

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