package com.pavlov.media.keycloak.testcontainer;

import com.pavlov.media.serviceKeycloak.PredefinedRolesConfig;
import com.pavlov.media.serviceKeycloak.service.KeycloakSetupService;
import com.pavlov.media.serviceKeycloak.service.RoleService;
import com.pavlov.media.serviceKeycloak.service.UserService;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class KeycloakTestContainerTest {

    @Container
    private static final GenericContainer<?> keycloakContainer =
            new GenericContainer<>("quay.io/keycloak/keycloak:26.3.1")
                    .withExposedPorts(8080)
                    .withEnv("KEYCLOAK_ADMIN", "admin")
                    .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                    .withEnv("KC_HOSTNAME_STRICT", "false")
                    .withEnv("KC_HTTP_ENABLED", "true")
                    .withCommand("start-dev");

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private Keycloak keycloak;
    @Autowired
    private KeycloakSetupService keycloakSetupService;
    @Autowired
    private PredefinedRolesConfig roleConfig;

    private final String REALM = "scassets";
    private final String CLIENT = "scassets-client";

    @DynamicPropertySource
    static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        String serverUrl = "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080);
        System.out.println("Setting Keycloak URL to: " + serverUrl);

        registry.add("keycloak.server-url", () -> serverUrl);
        registry.add("keycloak.realm", () -> "master");
        registry.add("keycloak.client-id", () -> "admin-cli");
        registry.add("keycloak.username", () -> "admin");
        registry.add("keycloak.password", () -> "admin");
        registry.add("keycloak.target-realm", () -> "scassets");
    }

    @Test
    void initTest() {
        assertTrue(keycloakSetupService.realmExists());
    }

    @Test
    void createRealmAndClientAndRoles_ShouldCreateAndVerify() {
        assertFalse(keycloakSetupService.realmExists());
//        try (Response response = keycloakSetupService.createRealmWithClient()) {
        keycloakSetupService.createRealmWithClient();
//            assertEquals(CREATED.getStatusCode(), response.getStatus());
            assertTrue(keycloakSetupService.realmExists());
            RealmResource realmResource = keycloak.realm(REALM);
            ClientsResource clientsResource = realmResource.clients();
            assertNotNull(keycloakSetupService.findClientByClientId(clientsResource));
//        }
        List<RoleRepresentation> allRealmRoles = roleService.getAllRoles();
        List<PredefinedRolesConfig.RoleConfig> predefinedRoleConfigs = roleService.getPredefinedRoleConfigs();
        List<String> predefinedNames = predefinedRoleConfigs.stream().map(PredefinedRolesConfig.RoleConfig::getName).toList();
        assertTrue(allRealmRoles.stream().map(RoleRepresentation::getName).noneMatch(name1 -> predefinedNames.stream().anyMatch(name1::contains)));

        roleService.createMissingPredefinedRoles();
        for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : roleConfig.getCompositeRoles()) {
            assertTrue(roleService.roleExists(compositeConfig.getName()));
        }
        for (PredefinedRolesConfig.RoleConfig simpleRoleConfig : roleConfig.getRoles()) {
            assertTrue(roleService.roleExists(simpleRoleConfig.getName()));
        }
    }

}
