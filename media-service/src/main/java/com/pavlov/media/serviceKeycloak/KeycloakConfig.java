package com.pavlov.media.serviceKeycloak;

import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@EnableConfigurationProperties(PredefinedRolesConfig.class)
public class KeycloakConfig {

    @Value("${keycloak.server-url:http://localhost:8089}")
    private String serverUrl;

    @Value("${keycloak.realm:master}")
    private String realm;

    @Value("${keycloak.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.username:admin}")
    private String username;

    @Value("${keycloak.password:admin}")
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
    public RealmResource realmResource(Keycloak keycloak,
                                       @Value("${keycloak.target-realm:scassets}") String targetRealm) {
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
    @ConditionalOnProperty(name = "keycloak.predefined-roles.auto-create", havingValue = "true")
    public CommandLineRunner initializePredefinedRoles(PredefinedRolesConfig rolesConfig, RolesResource rolesResource, @Value("${keycloak.target-realm:scassets}") String targetRealm) {
        return args -> {
            log.info("Initializing predefined roles for realm: {}", targetRealm);
            for (PredefinedRolesConfig.RoleConfig roleConfig : rolesConfig.getRoles()) {
                try {
                    rolesResource.get(roleConfig.getName()).toRepresentation();
                    log.debug("Role '{}' already exists, skipping", roleConfig.getName());
                } catch (NotFoundException e) {
                    RoleRepresentation role = new RoleRepresentation();
                    role.setName(roleConfig.getName());
                    role.setDescription(roleConfig.getDescription());
                    rolesResource.create(role);
                    log.info("Created predefined role: {}", roleConfig.getName());
                }
            }

            // Создаём композитные роли
            for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : rolesConfig.getCompositeRoles()) {
                try {
                    // Проверяем, существует ли уже композитная роль
                    RoleResource existingRole = rolesResource.get(compositeConfig.getName());
                    RoleRepresentation existing = existingRole.toRepresentation();

                    if (existing.isComposite()) {
                        log.debug("Composite role '{}' already exists, skipping", compositeConfig.getName());
                        continue;
                    }
                } catch (NotFoundException e) {
                    // Композитная роль не существует - создаём
                    RoleRepresentation compositeRole = new RoleRepresentation();
                    compositeRole.setName(compositeConfig.getName());
                    compositeRole.setDescription(compositeConfig.getDescription());
                    compositeRole.setComposite(true);
                    rolesResource.create(compositeRole);
                    log.info("Created composite role: {}", compositeConfig.getName());

                    // Добавляем вложенные роли
                    RoleResource newCompositeRole = rolesResource.get(compositeConfig.getName());
                    List<RoleRepresentation> includedRoles = compositeConfig.getIncludedRoles().stream()
                            .map(roleName -> {
                                try {
                                    return rolesResource.get(roleName).toRepresentation();
                                } catch (NotFoundException ex) {
                                    log.warn("Role '{}' not found for composite role '{}'",
                                            roleName, compositeConfig.getName());
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (!includedRoles.isEmpty()) {
                        newCompositeRole.addComposites(includedRoles);
                        log.info("Added {} roles to composite role '{}'",
                                includedRoles.size(), compositeConfig.getName());
                    }
                }
            }

            log.info("Predefined roles initialization completed");
        };
    }
}