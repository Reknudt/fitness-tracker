package com.pavlov.media.keycloak;

import com.pavlov.media.serviceKeycloak.service.KeycloakSetupService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RealmClientSetupTest {

    @Autowired
    private Keycloak keycloak;
    @Autowired
    private KeycloakSetupService keycloakSetupService;
    private final String REALM = "scassets";
    private final String CLIENT = "scassets-client";

    @Test
    @DisplayName("Should create realm and client")
    void createRealm_ShouldCreateRealmAndClient() {
//        try (Response response = keycloakSetupService.createRealmWithClient()) {
        try {
            keycloakSetupService.createRealmWithClient();
//                assertEquals(CREATED.getStatusCode(), response.getStatus());
            assertTrue(keycloakSetupService.realmExists());
            RealmResource realmResource = keycloak.realm(REALM);
            ClientsResource clientsResource = realmResource.clients();
            assertNotNull(keycloakSetupService.findClientByClientId(clientsResource));
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
//            realmResource.remove();
        }
    }

    @Test
    @DisplayName("Should throw conflict")
    void createRealm_ShouldThrowConflict() {
//        try (Response response = keycloakSetupService.createRealmWithClient()) {
        try {
            keycloakSetupService.createRealmWithClient();
//            assertEquals(CREATED.getStatusCode(), response.getStatus());

            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> keycloakSetupService.createRealmWithClient());
            assertEquals("Client already set up", e.getReason());
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
            realmResource.remove();
        }
    }

    /*@Test
    @DisplayName("Should reset realm and set client")
    void createRealm_ShouldResetRealmAndSetClient() {
        try (Response response = keycloakSetupService.createRealmWithClient()) {
            assertEquals(CREATED.getStatusCode(), response.getStatus());
            String location = response.getLocation().getPath();
            keycloak.realm(REALM).clients().get(location.substring(location.lastIndexOf('/') + 1)).remove();

            keycloakSetupService.createRealmWithClient();

            assertTrue(keycloakSetupService.realmExists());
            RealmResource realmResource = keycloak.realm(REALM);
            ClientsResource clientsResource = realmResource.clients();
            assertNotNull(keycloakSetupService.findClientByClientId(clientsResource));
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
            realmResource.remove();
        }
    }*/

    //    @Test
    @DisplayName("Remove REALM, optional test")
    void removeRealm() {
        RealmResource realmResource = keycloak.realm(REALM);
        realmResource.remove();
    }

}
