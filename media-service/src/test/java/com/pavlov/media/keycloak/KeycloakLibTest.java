package com.pavlov.media.keycloak;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KeycloakLibTest {

    private static Keycloak keycloak;
    private static RealmResource realmResource;
    private static RolesResource rolesResource;
    private static UsersResource usersResource;

    private static final String SERVER_URL = "http://localhost:8089";
    private static final String REALM = "master";
    private static final String CLIENT_ID = "admin-cli";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private final String TEST_REALM = "test-realm";

    @BeforeAll
    static void setUp() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .clientId(CLIENT_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        realmResource = keycloak.realm("demo-realm");
        rolesResource = realmResource.roles();
        usersResource = realmResource.users();
    }

    @Test
    void createRealm() {
        RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setRealm(TEST_REALM);
        newRealm.setEnabled(true);
        newRealm.setDisplayName("My Test Realm");
        newRealm.setLoginWithEmailAllowed(false);

        // Установка времени жизни токенов (важно для MicroProfile JWT)
        newRealm.setAccessTokenLifespan(300); // 5 минут
        newRealm.setSsoSessionMaxLifespan(36000); // 10 часов

        keycloak.realms().create(newRealm);
    }

    @Test
    void createNewRole() {
        String roleName = "TEST_ROLE_A";

        RoleRepresentation newRole = new RoleRepresentation();
        newRole.setName(roleName);
        newRole.setDescription("Тестовая роль");
        newRole.setComposite(true);

        rolesResource.create(newRole);

        RoleResource roleResource = rolesResource.get(roleName);
        RoleRepresentation createdRole = roleResource.toRepresentation();

        assertNotNull(createdRole);
        assertEquals(roleName, createdRole.getName());
        assertEquals("Тестовая роль", createdRole.getDescription());

//        rolesResource.deleteRole(roleName);
    }

    @Test
    void deleteRole() {
        String roleName = "TEST_ROLE_A";
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        rolesResource.create(role);

        assertNotNull(rolesResource.get(roleName).toRepresentation());

        rolesResource.deleteRole(roleName);

        assertThrows(Exception.class, () -> rolesResource.get(roleName).toRepresentation());
    }

    @Test
    void getListOfRoles() {
        List<RoleRepresentation> roles = rolesResource.list();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
    }

    @Test
    void updateRole() {
        String roleName = "TEST_ROLE_A";

        // Создаем роль
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription("Исходное описание");
        rolesResource.create(role);

        // Обновляем роль
        RoleResource roleResource = rolesResource.get(roleName);
        RoleRepresentation roleToUpdate = roleResource.toRepresentation();
        roleToUpdate.setDescription("Обновленное описание");
        roleToUpdate.setComposite(true);    // not working without associated roles
        roleResource.update(roleToUpdate);

        // Проверяем обновление
        RoleRepresentation updatedRole = roleResource.toRepresentation();
        assertEquals("Обновленное описание", updatedRole.getDescription());
//        assertTrue(updatedRole.isComposite());

//        rolesResource.deleteRole(roleName);
    }

    @Test
    void createCompositeRole() {
        // Создаем базовые роли
        String baseRole1 = "BASE_ROLE_1";
        String baseRole2 = "BASE_ROLE_2";
        String compositeRoleName = "COMPOSITE_ROLE";

        createRole(baseRole1);
        createRole(baseRole2);

        // Создаем композитную роль
        RoleRepresentation compositeRole = new RoleRepresentation();
        compositeRole.setName(compositeRoleName);
        compositeRole.setComposite(true);
        rolesResource.create(compositeRole);

        // Получаем базовые роли
        RoleRepresentation role1 = rolesResource.get(baseRole1).toRepresentation();
        RoleRepresentation role2 = rolesResource.get(baseRole2).toRepresentation();

        // Добавляем базовые роли в композитную
        RoleResource compositeRoleResource = rolesResource.get(compositeRoleName);
        compositeRoleResource.addComposites(Arrays.asList(role1, role2));

        // Проверяем композитные роли
        Set<RoleRepresentation> composites = compositeRoleResource.getRoleComposites();
        assertEquals(2, composites.size());

//        rolesResource.deleteRole(compositeRoleName);
//        rolesResource.deleteRole(baseRole1);
//        rolesResource.deleteRole(baseRole2);
    }

    @Test
    void createUser() {
        //----- 1 block, create user

        UserRepresentation user = new UserRepresentation();
        String username = "test-user";
        user.setUsername(username);
        user.setEmail("TESTUSER@test.com");
        user.setEnabled(true);
        user.setAttributes(Map.of("fullname", List.of("d1"), "position", List.of("tester")));
        user.setRequiredActions(List.of("UPDATE_PASSWORD"));

        Response response = usersResource.create(user);
        assertEquals(201, response.getStatus());

        // --------- 2 block, find that user

        List<UserRepresentation> users = usersResource.search(username);
        assertEquals(username, users.getFirst().getUsername());

        String userId = users.getFirst().getId();
        UserResource userResource = usersResource.get(userId);

        // ---------- 3 block, set temporary password

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        String tempPass = "123";
        credential.setValue(tempPass);
        credential.setTemporary(true);

        userResource.resetPassword(credential);

        // ----------- 4 block, set require action UPDATE PASSWORD

        UserRepresentation userUpdate = userResource.toRepresentation();
        userUpdate.setRequiredActions(List.of("UPDATE_PASSWORD"));
        userResource.update(userUpdate);

        // now authenticate and refresh password

        // Act & Assert - проверяем аутентификацию
//        try {
//            // Попытка получить токен через KeycloakBuilder
//            Keycloak userKeycloak = KeycloakBuilder.builder()
//                    .serverUrl(SERVER_URL)
//                    .realm(REALM)
//                    .clientId(CLIENT_ID)
//                    .username(username)
//                    .password(tempPass)
//                    .build();
//
//            // Эта строка может вести себя по-разному в зависимости от конфигурации
//            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();
//
//            // Если мы получили токен - проверяем REQUIRED_ACTION
//            System.out.println("Token получен: " + tokenResponse.getToken());
//
//            // Проверяем что REQUIRED_ACTION все еще установлен
//            UserRepresentation currentUser = userResource.toRepresentation();
//            assertTrue(currentUser.getRequiredActions().contains("UPDATE_PASSWORD"),
//                    "UPDATE_PASSWORD should still be required");
//
//        } catch (Exception e) {
//            // Или получаем исключение - зависит от конфигурации Keycloak
//            System.out.println("Аутентификация отклонена: " + e.getMessage());
//            assertTrue(e.getMessage().contains("401") || e.getMessage().contains("temporary"));
//        }
    }

    @Test
    void assignRoleToUser() {
        // Создаем пользователя
        String username = "testuser_" + System.currentTimeMillis();
        String userId = createUser(username, "password123");

        // Создаем роль
        String roleName = "USER_ROLE";
        createRole(roleName);

        // Назначаем роль пользователю
        UserResource userResource = usersResource.get(userId);
        RoleRepresentation role = rolesResource.get(roleName).toRepresentation();

        userResource.roles().realmLevel().add(Collections.singletonList(role));

        // Проверяем назначенные роли
        List<RoleRepresentation> userRoles = userResource.roles().realmLevel().listAll();
        boolean roleAssigned = userRoles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
        assertTrue(roleAssigned);

//        userResource.remove();
//        rolesResource.deleteRole(roleName);
    }

    // ------- user tests

    @Test
    void createUserAccount() {
        String username = "newuser_" + System.currentTimeMillis();
        String email = username + "@test.com";

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Создаем пользователя
        Response response = usersResource.create(user);
        assertEquals(201, response.getStatus());

        // Получаем ID созданного пользователя
//        String userId = getCreatedId(response);
//        assertNotNull(userId);


        // Устанавливаем пароль     ---
        UserRepresentation userRepresentation = usersResource.search(username).getFirst();
        UserResource userResource = usersResource.get(userRepresentation.getId());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("password123");
        credential.setTemporary(false);
        userResource.resetPassword(credential);

//         Проверяем создание
        UserRepresentation createdUser = userResource.toRepresentation();
        assertEquals(username, createdUser.getUsername());
        assertEquals(email, createdUser.getEmail());
        assertTrue(createdUser.isEnabled());

        // Cleanup
//        userResource.remove();
    }

    @Test
    void enableDisableUser() {
        // Создаем пользователя
        String username = "toggleuser_" + System.currentTimeMillis();
        String userId = createUser(username, "password123");

        UserResource userResource = usersResource.get(userId);

        // Блокируем пользователя
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);

        // Проверяем блокировку
        UserRepresentation disabledUser = userResource.toRepresentation();
        assertFalse(disabledUser.isEnabled());
//
//        // Разблокируем пользователя
//        user.setEnabled(true);
//        userResource.update(user);
//
//        // Проверяем разблокировку
//        UserRepresentation enabledUser = userResource.toRepresentation();
//        assertTrue(enabledUser.isEnabled());
//
//        // Cleanup
//        userResource.remove();
    }

    @Test
    void updateUserProfile() {
        // Создаем пользователя
        String username = "updateuser_" + System.currentTimeMillis();
        String userId = createUser(username, "password123");

        UserResource userResource = usersResource.get(userId);

        // Обновляем профиль
        UserRepresentation user = userResource.toRepresentation();
        user.setFirstName("UpdatedFirstName");
        user.setLastName("UpdatedLastName");
        user.setEmail("updated_" + username + "@test.com");
        userResource.update(user);

        // Проверяем обновление
        UserRepresentation updatedUser = userResource.toRepresentation();
//        assertEquals("UpdatedFirstName", updatedUser.getFirstName());
//        assertEquals("UpdatedLastName", updatedUser.getLastName());
        assertEquals("updated_" + username + "@test.com", updatedUser.getEmail());

//        // Cleanup
//        userResource.remove();
    }

    @Test
    void searchUsers() {
        // Создаем тестового пользователя
        String username = "searchuser_" + System.currentTimeMillis();
        createUser(username, "password123");

        // Ищем пользователя
        List<UserRepresentation> users = usersResource.search(username);
        assertFalse(users.isEmpty());
        assertEquals(username, users.get(0).getUsername());

        System.out.println(users);

        // Cleanup
        String userId = users.get(0).getId();
        usersResource.get(userId).remove();
    }

    @Test
    void getUserRoles() {
        // Создаем пользователя и роль
        String username = "roleuser_" + System.currentTimeMillis();
        String userId = createUser(username, "password123");
        String roleName = "TEST_ROLE_FOR_USER";
        createRole(roleName);

        UserResource userResource = usersResource.get(userId);
        RoleRepresentation role = rolesResource.get(roleName).toRepresentation();

        // Назначаем роль
        userResource.roles().realmLevel().add(Arrays.asList(role));

        // Получаем роли пользователя
        List<RoleRepresentation> userRoles = userResource.roles().realmLevel().listAll();
        List<RoleRepresentation> effectiveRoles = userResource.roles().realmLevel().listEffective();

        assertNotNull(userRoles);
        assertNotNull(effectiveRoles);

        // Cleanup
//        userResource.remove();
//        rolesResource.deleteRole(roleName);
    }

    @Test
    void removeRoleFromUser() {
        // Создаем пользователя и роль
        String username = "removeroleuser_" + System.currentTimeMillis();
        String userId = createUser(username, "password123");
        String roleName = "ROLE_TO_REMOVE";
        createRole(roleName);

        UserResource userResource = usersResource.get(userId);
        RoleRepresentation role = rolesResource.get(roleName).toRepresentation();

        // Назначаем роль
        userResource.roles().realmLevel().add(Collections.singletonList(role));

        // Удаляем роль
        userResource.roles().realmLevel().remove(Collections.singletonList(role));

        // Проверяем удаление
        List<RoleRepresentation> userRoles = userResource.roles().realmLevel().listAll();
        boolean roleStillExists = userRoles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
        assertFalse(roleStillExists);

        // Cleanup
        userResource.remove();
        rolesResource.deleteRole(roleName);
    }

    //  -------------

    @Test
    public void createUser1() {
        UserRepresentation user = new UserRepresentation();
        user.setUsername("test");
        user.setEnabled(true);
        user.setAttributes(Map.of("startedOn", List.of("2024-02-23")));
        usersResource.create(user);
    }

    private String createUser(String username, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setEnabled(true);
        user.setAttributes(Map.of("fullname", List.of("d1"), "position", List.of("tester")));

        /*Response response =*/ usersResource.create(user);



//        UserRepresentation userResponse = response.readEntity(UserRepresentation.class);
//        String id = userResponse.getId();
//        System.out.println("user response " + id);

//        String userId = getCreatedId(response);

//        UserRepresentation userRepresentation = usersResource.search(username).getFirst();
//        UserResource userResource = usersResource.get(userRepresentation.getId());

        UserRepresentation userRepresentation = usersResource.search(username).getFirst();
        String id = userRepresentation.getId();
        UserResource userResource = usersResource.get(id);
//        UserResource userResource = usersResource.get(id);

        // Устанавливаем пароль
//        UserResource userResource = usersResource.get(userId);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        userResource.resetPassword(credential);

        return id;
    }

    private void createRole(String roleName) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        rolesResource.create(role);
    }

    private String getCreatedId(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

//    @AfterEach
//    void cleanup() {
//        // Дополнительная очистка если нужна
//        if (keycloak != null) {
//            keycloak.close();
//        }
//    }
//}

    // ---- only roles

    @Test
    void testDifferentRoleTypes() {
        // 1. Realm role
        RoleRepresentation realmRole = new RoleRepresentation();
        realmRole.setName("realm-viewer");
        rolesResource.create(realmRole);

        // 2. Client role
        String clientId = "test-client";
        ClientResource client = realmResource.clients().get(clientId);
        RoleRepresentation clientRole = new RoleRepresentation();
        clientRole.setName("client-admin");
        client.roles().create(clientRole);

        // 3. Composite role
        RoleRepresentation compositeRole = new RoleRepresentation();
        compositeRole.setName("power-user");
        compositeRole.setComposite(true);
        rolesResource.create(compositeRole);

        // Добавляем роли в композитную
        RoleResource powerUserRole = rolesResource.get("power-user");
        powerUserRole.addComposites(Arrays.asList(
                rolesResource.get("realm-viewer").toRepresentation(),
                client.roles().get("client-admin").toRepresentation()
        ));

        // 4. Group with roles
        GroupRepresentation group = new GroupRepresentation();
        group.setName("power-users-group");
        realmResource.groups().add(group);

        // Назначаем композитную роль группе
        GroupResource groupResource = realmResource.groups().group(group.getId());
        groupResource.roles().realmLevel().add(Arrays.asList(
                rolesResource.get("power-user").toRepresentation()
        ));

        // Проверяем, что композитная роль включает нужные роли
        Set<RoleRepresentation> composites = powerUserRole.getRoleComposites();
        assertEquals(2, composites.size());
    }

}

