package com.pavlov.media.serviceKeycloak;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(PredefinedRolesConfig.class)
public class KeycloakConfig {

    private final PredefinedRolesConfig rolesConfig;

    @Value("${keycloak.target.realm}")
    private String targetRealm;

    @Value("${keycloak.target.client-id}")
    private String targetClientId;

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

    // Default user config
    @Value("${keycloak.default-user.username}")
    private String defaultUsername;

    @Value("${keycloak.default-user.email}")
    private String defaultEmail;

    @Value("${keycloak.default-user.password}")
    private String defaultPassword;

    @Value("${keycloak.default-user.firstName}")
    private String defaultFirstName;

    @Value("${keycloak.default-user.lastName}")
    private String defaultLastName;

    @Value("${keycloak.default-user.roles}")
    private List<String> defaultUserRoles;

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

    @ConditionalOnProperty(name = "keycloak.auto-create", havingValue = "true")
    @EventListener(ApplicationReadyEvent.class)
    public void initializePredefinedRealm(ApplicationReadyEvent event) {
        log.info("Starting Keycloak environment initialization");
        Keycloak keycloak = keycloak();

        RealmResource realmResource = ensureRealmExists(keycloak);
        ensureClientExists(realmResource);
        ensurePredefinedRolesExist(realmResource.roles());
        ensureDefaultUserExists(realmResource.users(), realmResource.roles());
        log.info("Keycloak environment ensured successfully");

//        RealmResource realmResource = keycloak.realm(targetRealm);
//        RolesResource rolesResource = realmResource.roles();
//        UsersResource usersResource = realmResource.users();

        /*if (realmExists(keycloak)) {
            log.info("Predefined realm: {} already exists", targetRealm);
        } else {
            log.info("Initializing predefined realm: {}", targetRealm);
            createRealm(keycloak);

            log.info("Initializing predefined roles for realm: {}", targetRealm);
            createPredefinedRoles(rolesResource);
            log.info("Initializing default user '{}' for realm: {}", defaultUsername, targetRealm);
            createDefaultUser(usersResource, rolesResource);
        }*/
    }

    private RealmResource ensureRealmExists(Keycloak keycloak) {
        try {
            RealmResource realmResource = keycloak.realms().realm(targetRealm);
            realmResource.toRepresentation();
            log.info("Realm '{}' exists", targetRealm);
            return realmResource;
        } catch (NotFoundException e) {
            log.info("Creating realm '{}'...", targetRealm);
            createRealm(keycloak);
            return keycloak.realms().realm(targetRealm);
        }
    }

    private void ensureClientExists(RealmResource realmResource) {
        List<ClientRepresentation> clients = realmResource.clients().findByClientId(targetClientId);
        if (!clients.isEmpty()) {
            log.info("Client '{}' exists", targetClientId);
            return;
        }
        log.info("Creating client '{}'...", targetClientId);
        createClient(realmResource);
    }

    private void ensureDefaultUserExists(UsersResource usersResource, RolesResource rolesResource) {
        List<UserRepresentation> existingUsers = usersResource.search(defaultUsername);
        if (!existingUsers.isEmpty()) {
            log.info("Default user '{}' exists", defaultUsername);
            ensureUserHasRequiredRoles(existingUsers.getFirst().getId(), usersResource, rolesResource);
            return;
        }
        log.info("Creating default user '{}'...", defaultUsername);
        createDefaultUser(usersResource, rolesResource);
    }

    private void ensureUserHasRequiredRoles(String userId, UsersResource usersResource, RolesResource rolesResource) {
        UserResource userResource = usersResource.get(userId);

        List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();
        Set<String> currentRoleNames = currentRoles.stream().map(RoleRepresentation::getName).collect(Collectors.toSet());

        List<RoleRepresentation> missingRoles = defaultUserRoles.stream()
                .filter(roleName -> !currentRoleNames.contains(roleName))
                .map(roleName -> {
                    try {
                        return rolesResource.get(roleName).toRepresentation();
                    } catch (NotFoundException e) {
                        log.warn("Role '{}' not found for default user", roleName);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!missingRoles.isEmpty()) {
            userResource.roles().realmLevel().add(missingRoles);
            log.info("Added {} missing roles to default user '{}'", missingRoles.size(), defaultUsername);
        }
    }

    /*private boolean realmExists(Keycloak keycloak) {
        try {
            keycloak.realms().realm(targetRealm).toRepresentation();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }*/

//    private boolean clientExists(Keycloak keycloak) {
//        try {
//            keycloak.realms().realm(targetRealm).clients().get(targetClientId);
//            return true;
//        } catch (NotFoundException e) {
//            return false;
//        }
//    }

//    private boolean defaultUserExists(UsersResource usersResource) {
//        return Objects.nonNull(usersResource.search(defaultUsername).getFirst());
//    }

    public void createRealm(Keycloak keycloak) {
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(targetRealm);
        realmRepresentation.setEnabled(true);
        keycloak.realms().create(realmRepresentation);
    }

    /*private void setRealmScopeMicroproflieToDefault(Keycloak keycloak) {
        List<ClientScopeRepresentation> optionalClientScopeRepresentations = keycloak.realms().realm(targetRealm).getDefaultOptionalClientScopes();

        String microprofileScopeId = optionalClientScopeRepresentations.stream()
                .filter(s -> "microprofile-jwt".equals(s.getName()))
                .findFirst()
                .map(ClientScopeRepresentation::getId)
                .orElseThrow(() -> new RuntimeException("Scope not found: " + "microprofile-jwt"));     //

        keycloak.realms().realm(targetRealm).removeDefaultOptionalClientScope(microprofileScopeId);
        keycloak.realms().realm(targetRealm).addDefaultDefaultClientScope(microprofileScopeId);
    } */

    private void createClient(RealmResource realmResource) {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(targetClientId);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setDefaultClientScopes(List.of("web-origins", "acr", "profile", "roles", "user-profile-attributes", "microprofile-jwt", "basic", "email"));
        clientRepresentation.setOptionalClientScopes(List.of("address", "phone", "organization", "offline_access"));
        clientRepresentation.setRedirectUris(List.of("*"));
        clientRepresentation.setWebOrigins(List.of("*"));
        clientRepresentation.setAttributes(Map.of("post.logout.redirect.uris", "+"));
        try (Response response = realmResource.clients().create(clientRepresentation)) {
            if (Response.Status.CREATED.getStatusCode() != response.getStatus())
                throw new RuntimeException("Error occurred with code " + response.getStatus() + " and message: " + response);
        }
    }

    public void createPredefinedRoles(RolesResource rolesResource) {
        for (PredefinedRolesConfig.RoleConfig roleConfig : rolesConfig.getRoles()) {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleConfig.getName());
            role.setDescription(roleConfig.getDescription());
            rolesResource.create(role);
            log.info("Created predefined role: {}", roleConfig.getName());
        }
        for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : rolesConfig.getCompositeRoles()) {
            RoleRepresentation compositeRole = new RoleRepresentation();
            compositeRole.setName(compositeConfig.getName());
            compositeRole.setDescription(compositeConfig.getDescription());
            compositeRole.setComposite(true);
            rolesResource.create(compositeRole);
            log.info("Created composite role: {}", compositeConfig.getName());

            RoleResource newCompositeRole = rolesResource.get(compositeConfig.getName());
            List<RoleRepresentation> includedRoles = compositeConfig.getIncludedRoles().stream()
                    .map(roleName -> {
                        try {
                            return rolesResource.get(roleName).toRepresentation();
                        } catch (NotFoundException ex) {
                            log.warn("Role '{}' not found for composite role '{}'", roleName, compositeConfig.getName());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!includedRoles.isEmpty()) {
                newCompositeRole.addComposites(includedRoles);
                log.info("Added {} roles to composite role '{}'", includedRoles.size(), compositeConfig.getName());
            }
        }
    }

    public void createDefaultUser(UsersResource usersResource, RolesResource rolesResource) {
        UserRepresentation defaultUser = new UserRepresentation();
        defaultUser.setUsername(defaultUsername);
        defaultUser.setEmail(defaultEmail);
        defaultUser.setEnabled(true);
        defaultUser.setFirstName(defaultFirstName);
        defaultUser.setLastName(defaultLastName);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(defaultUsername);
        credential.setTemporary(false);
        defaultUser.setCredentials(List.of(credential));

        Response response = usersResource.create(defaultUser);
        String location = response.getLocation().getPath();
        String userId = location.substring(location.lastIndexOf('/') + 1);  // get userId from url

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

        usersResource.get(userId).roles().realmLevel().add(roles);
    }

    private void ensurePredefinedRolesExist(RolesResource rolesResource) {
        log.info("Ensuring predefined roles exist...");
        for (PredefinedRolesConfig.RoleConfig roleConfig : rolesConfig.getRoles()) {
            ensureRoleExists(rolesResource, roleConfig.getName(), roleConfig.getDescription(), false);
        }
        for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : rolesConfig.getCompositeRoles()) {
            ensureCompositeRoleExists(rolesResource, compositeConfig);
        }
    }

    private void ensureRoleExists(RolesResource rolesResource, String roleName, String description, boolean isComposite) {
        try {
            rolesResource.get(roleName).toRepresentation();
            log.debug("Role '{}' already exists", roleName);
        } catch (NotFoundException e) {
            log.info("Creating role '{}'...", roleName);
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);
            role.setComposite(isComposite);
            rolesResource.create(role);
        }
    }

    private void ensureCompositeRoleExists(RolesResource rolesResource, PredefinedRolesConfig.CompositeRoleConfig config) {
        ensureRoleExists(rolesResource, config.getName(), config.getDescription(), true);
        try {
            RoleResource compositeRole = rolesResource.get(config.getName());
            Set<String> existingComposites = compositeRole.getRoleComposites().stream().map(RoleRepresentation::getName).collect(Collectors.toSet());
            List<RoleRepresentation> rolesToAdd = config.getIncludedRoles().stream()
                    .filter(roleName -> !existingComposites.contains(roleName))
                    .map(roleName -> {
                        try {
                            return rolesResource.get(roleName).toRepresentation();
                        } catch (NotFoundException e) {
                            log.warn("Role '{}' not found for composite role '{}'", roleName, config.getName());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!rolesToAdd.isEmpty()) {
                compositeRole.addComposites(rolesToAdd);
                log.info("Added {} roles to composite role '{}'", rolesToAdd.size(), config.getName());
            }
        } catch (NotFoundException e) {
            log.error("Composite role '{}' not found after creation", config.getName());
        }
    }
}