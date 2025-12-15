package com.pavlov.media.keycloak;

import com.pavlov.media.serviceKeycloak.PredefinedRolesConfig;
import com.pavlov.media.serviceKeycloak.request.RoleRequest;
import com.pavlov.media.serviceKeycloak.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoleServiceTest {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PredefinedRolesConfig roleConfig;

    private String generateUniqueRoleName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Should create new role and verify it exists")
    void createRole_WithValidData_ShouldCreateRole() {
        // Arrange
        String roleName = generateUniqueRoleName("TEST_ROLE_1");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);
        request.setDescription("Test role description");
        request.setComposite(false);

        try {
            roleService.createRole(request);

            assertTrue(roleService.roleExists(roleName));
            RoleRepresentation createdRole = roleService.getRoleByName(roleName);
            assertNotNull(createdRole);
            assertEquals(roleName, createdRole.getName());
            assertEquals("Test role description", createdRole.getDescription());
        } finally {
//            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should get all roles and verify new role is included")
    void getAllRoles_ShouldIncludeNewlyCreatedRole() {
        String roleName = generateUniqueRoleName("ALL_ROLES_TEST");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);
        request.setDescription("Role for getAll test");

        try {
            roleService.createRole(request);
            List<RoleRepresentation> roles = roleService.getAllRoles();

            for (RoleRepresentation role : roles) {
                System.out.println("roles: " + role.getDescription());
            }

            assertNotNull(roles);
            assertFalse(roles.isEmpty());
            assertTrue(roles.stream().anyMatch(role -> roleName.equals(role.getName())));
        } finally {
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should return true when checking existence of created role")
    void roleExists_WhenRoleExists_ShouldReturnTrue() {
        String roleName = generateUniqueRoleName("EXISTS_TEST");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);

        try {
            roleService.createRole(request);
            assertTrue(roleService.roleExists(roleName));
        } finally {
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should return false when checking existence of non-existent role")
    void roleExists_WhenRoleNotExists_ShouldReturnFalse() {
        String nonExistentRole = generateUniqueRoleName("NON_EXISTENT");
        assertFalse(roleService.roleExists(nonExistentRole));
    }

    @Test
    @DisplayName("Should update role description successfully")
    void updateRole_WithNewDescription_ShouldUpdateRole() {
        String roleName = generateUniqueRoleName("UPDATE_TEST");
        RoleRequest createRequest = new RoleRequest();
        createRequest.setName(roleName);
        createRequest.setDescription("Original description");

        try {
            roleService.createRole(createRequest);

            RoleRequest updateRequest = new RoleRequest();
            updateRequest.setDescription("Updated description");
            roleService.updateRole(roleName, updateRequest);

            RoleRepresentation updatedRole = roleService.getRoleByName(roleName);
            assertEquals("Updated description", updatedRole.getDescription());
            assertEquals(roleName, updatedRole.getName()); // Name should remain unchanged
        } finally {
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should create composite role with child roles")
    void createRole_WithCompositeRole_ShouldCreateWithComposites() {
        String parentRoleName = generateUniqueRoleName("COMPOSITE_PARENT");
        String childRole1 = generateUniqueRoleName("CHILD_1");
        String childRole2 = generateUniqueRoleName("CHILD_2");

        try {
            // Create child roles first
            RoleRequest childRequest1 = new RoleRequest();
            childRequest1.setName(childRole1);
            childRequest1.setDescription("First child role");
            roleService.createRole(childRequest1);

            RoleRequest childRequest2 = new RoleRequest();
            childRequest2.setName(childRole2);
            childRequest2.setDescription("Second child role");
            roleService.createRole(childRequest2);

            // Create composite role
            RoleRequest compositeRequest = new RoleRequest();
            compositeRequest.setName(parentRoleName);
            compositeRequest.setDescription("Composite role with children");
            compositeRequest.setComposite(true);
            compositeRequest.setCompositeRoles(List.of(childRole1, childRole2));

            roleService.createRole(compositeRequest);

            assertTrue(roleService.roleExists(parentRoleName));
            RoleRepresentation compositeRole = roleService.getRoleByName(parentRoleName);
            assertNotNull(compositeRole);
            assertEquals(parentRoleName, compositeRole.getName());
            assertTrue(compositeRole.isComposite());
        } finally {
            safeDeleteRole(parentRoleName);
            safeDeleteRole(childRole1);
            safeDeleteRole(childRole2);
        }
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate role")
    void createRole_WhenRoleAlreadyExists_ShouldThrowException() {
        String roleName = generateUniqueRoleName("DUPLICATE_TEST");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);
        request.setDescription("Original role");

        try {
            roleService.createRole(request);
            assertTrue(roleService.roleExists(roleName));

            // try to create duplicate
            RoleRequest duplicateRequest = new RoleRequest();
            duplicateRequest.setName(roleName);
            duplicateRequest.setDescription("Duplicate role");

            RuntimeException exception = assertThrows(RuntimeException.class, () -> roleService.createRole(duplicateRequest));

            assertTrue(exception.getCause().getMessage().contains("already exists"));

        } finally {
            // Cleanup
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Create Role with no name")
    void createRoleWithNoName_shouldReturnBadRequest() {
        RoleRequest request = new RoleRequest();
        request.setDescription("should throw bad request");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roleService.createRole(request);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent role")
    void getRoleByName_WhenRoleNotExists_ShouldThrowException() {
        String nonExistentRole = generateUniqueRoleName("NON_EXISTENT_GET");
        assertThrows(RuntimeException.class, () -> roleService.getRoleByName(nonExistentRole));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent role")
    void updateRole_WhenRoleNotExists_ShouldThrowException() {
        String nonExistentRole = generateUniqueRoleName("NON_EXISTENT_UPDATE");
        RoleRequest updateRequest = new RoleRequest();
        updateRequest.setDescription("Some description");

        assertThrows(RuntimeException.class, () -> roleService.updateRole(nonExistentRole, updateRequest));
    }

    @Test
    @DisplayName("Should delete existing role successfully")
    void deleteRole_WhenRoleExists_ShouldDeleteRole() {
        String roleName = generateUniqueRoleName("DELETE_TEST");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);
        request.setDescription("Role to delete");

        // Create role first
        roleService.createRole(request);
        assertTrue(roleService.roleExists(roleName));

        roleService.deleteRole(roleName);

        assertFalse(roleService.roleExists(roleName));
        // Verify cannot get deleted role
        assertThrows(RuntimeException.class, () -> roleService.getRoleByName(roleName));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent role")
    void deleteRole_WhenRoleNotExists_ShouldThrowException() {
        String nonExistentRole = generateUniqueRoleName("NON_EXISTENT_DELETE");
        assertThrows(RuntimeException.class, () -> roleService.deleteRole(nonExistentRole));
    }

    @Test
    @DisplayName("Should create role with attributes")
    void createRole_WithAttributes_ShouldCreateRoleWithAttributes() {
        String roleName = generateUniqueRoleName("ATTR_TEST");
        RoleRequest request = new RoleRequest();
        request.setName(roleName);
        request.setDescription("Role with attributes");
        request.setAttributes(Map.of("department", List.of("IT"), "accessLevel", List.of("high")));

        try {
            roleService.createRole(request);

            RoleRepresentation role = roleService.getRoleByName(roleName);
            assertNotNull(role);
            assertEquals(roleName, role.getName());
            assertNotNull(role.getAttributes());
            assertTrue(role.getAttributes().containsKey("department"));
            assertTrue(role.getAttributes().containsKey("accessLevel"));
        } finally {
            safeDeleteRole(roleName);
        }
    }

    @Test
    @DisplayName("Should update role attributes")
    void updateRole_WithAttributes_ShouldUpdateAttributes() {
        String roleName = generateUniqueRoleName("UPDATE_ATTR_TEST");

        RoleRequest createRequest = new RoleRequest();
        createRequest.setName(roleName);
        createRequest.setDescription("Original role");
        roleService.createRole(createRequest);

        try {
            // Update request with attributes
            RoleRequest updateRequest = new RoleRequest();
            updateRequest.setAttributes(Map.of("updatedField", List.of("newValue"), "category", List.of("premium")));

            roleService.updateRole(roleName, updateRequest);

            RoleRepresentation updatedRole = roleService.getRoleByName(roleName);
            assertNotNull(updatedRole.getAttributes());
            assertTrue(updatedRole.getAttributes().containsKey("updatedField"));
            assertTrue(updatedRole.getAttributes().containsKey("category"));
        } finally {
            safeDeleteRole(roleName);
        }
    }

    // Helper method for safe cleanup
    private void safeDeleteRole(String roleName) {
        roleService.deleteRole(roleName);
    }

    //----------------------------- test predefined roles

    @Test
    @DisplayName("Should create predefined roles and verify it exists")
    void createPredefinedRoles_ShouldCreateRoles() {
        try {
            roleService.createMissingPredefinedRoles();
            for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : roleConfig.getCompositeRoles()) {
                assertTrue(roleService.roleExists(compositeConfig.getName()));
            }
            for (PredefinedRolesConfig.RoleConfig simpleRoleConfig : roleConfig.getRoles()) {
                assertTrue(roleService.roleExists(simpleRoleConfig.getName()));
            }
        } finally {
            deleteCreatedPredefinedRoles_ShouldDeletePredefinedRoles();
        }
    }

    @Test
    @DisplayName("Should delete predefined roles")
    void deleteCreatedPredefinedRoles_ShouldDeletePredefinedRoles() {
        for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : roleConfig.getCompositeRoles()) {
            roleService.deleteRole(compositeConfig.getName());
        }
        for (PredefinedRolesConfig.RoleConfig simpleRoleConfig : roleConfig.getRoles()) {
            roleService.deleteRole(simpleRoleConfig.getName());
        }
    }

    @Test
    @DisplayName("Should print predefined roles' names")
    void getPredefinedRoles() {
        List<PredefinedRolesConfig.RoleConfig> predefinedRoleConfigs = roleService.getPredefinedRoleConfigs();
        for (PredefinedRolesConfig.RoleConfig predefinedRoleConfig : predefinedRoleConfigs) {
            System.out.println("Roles: " + predefinedRoleConfig.getName() + " ---\n");
        }
        List<PredefinedRolesConfig.CompositeRoleConfig> predefinedCompositeRoleConfigs = roleService.getPredefinedCompositeRoleConfigs();
        for (PredefinedRolesConfig.CompositeRoleConfig predefinedCompositeRoleConfig : predefinedCompositeRoleConfigs) {
            System.out.println("Roles composite: " + predefinedCompositeRoleConfig.getName() + " ---\n");
        }
    }

}