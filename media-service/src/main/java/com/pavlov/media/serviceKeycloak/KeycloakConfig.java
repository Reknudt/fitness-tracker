package com.pavlov.media.serviceKeycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
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
                                       @Value("${keycloak.target-realm:depo-clone}") String targetRealm) {
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
}