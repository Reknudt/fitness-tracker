package com.pavlov.media.serviceKeycloak;

import com.pavlov.media.serviceKeycloak.service.KeycloakSetupService;
import com.pavlov.media.serviceKeycloak.service.RoleService;
import com.pavlov.media.serviceKeycloak.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
//@RequiredArgsConstructor    //
@EnableConfigurationProperties(PredefinedRolesConfig.class)
public class KeycloakConfig {

//    private final KeycloakSetupService keycloakSetupService;
//    private final UserService userService;
//    private final String REALM = "scassets";
//    private final String CLIENT = "scassets-client";

    @Value("${keycloak.target.realm}")
    private String targetRealm;

    @Value("${keycloak.target.client}")
    private String targetClient;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.username}")
    private String username;

    @Value("${keycloak.password}")
    private String password;

    @Bean
    @Primary
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public RealmResource realmResource(Keycloak keycloak, @Value("${keycloak.target.realm}") String targetRealm) {
        return keycloak.realm(targetRealm);
    }

    @Bean
    public RolesResource rolesResource(RealmResource realmResource) {
        return realmResource.roles();
    }

    @Bean
    public UsersResource usersResource(RealmResource realmResource) {
        return realmResource.users();
    }

    @Bean
    @ConditionalOnProperty(name = "keycloak.auto-create", havingValue = "true")
    public CommandLineRunner initializePredefinedRealm(PredefinedRolesConfig rolesConfig) {
        return args -> {

            log.info("Starting Keycloak environment initialization");

            // Создаём временный экземпляр Keycloak для инициализации
            Keycloak initKeycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .username(username)
                    .password(password)
                    .build();

            KeycloakSetupService setupService = new KeycloakSetupService(initKeycloak);
            UserService userService = new UserService(usersResource(initKeycloak.realm(targetRealm)), rolesResource(initKeycloak.realm(realm)));
            RoleService roleService = new RoleService(rolesResource(initKeycloak.realm(targetRealm)), rolesConfig);

            initializeEnvironment(setupService, userService, roleService, rolesConfig);
        };
    }

    private void initializeEnvironment(KeycloakSetupService setupService, UserService userService, RoleService roleService, PredefinedRolesConfig rolesConfig) {
        if (setupService.realmExists()) {
            log.info("Predefined realm: {} exists ", targetRealm);
        } else {
            log.info("Initializing predefined realm: {}", targetRealm);
            setupService.createRealmWithClient();

            log.info("Initializing predefined roles for realm: {}", targetRealm);
            roleService.createPredefinedRoles();

            log.info("Initializing default user 'root' for realm: {}", targetRealm);
            userService.createDefaultUser();
        }

    }
}