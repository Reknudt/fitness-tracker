package com.pavlov.media.serviceKeycloak.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class KeycloakSetupService {

    private final Keycloak keycloak;
    private final String REALM = "scassets";
    private final String CLIENT = "scassets-client";

    public void createRealmWithClient() {
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(REALM);
        realmRepresentation.setEnabled(true);
        keycloak.realms().create(realmRepresentation);
        configureClient();
    }

    /*private void setRealmScopeMicroproflieToDefault() {           //not necessary method
        List<ClientScopeRepresentation> optionalClientScopeRepresentations = keycloak.realms().realm(REALM).getDefaultOptionalClientScopes();

        String microprofileScopeId = optionalClientScopeRepresentations.stream()
                .filter(s -> "microprofile-jwt".equals(s.getName()))
                .findFirst()
                .map(ClientScopeRepresentation::getId)
                .orElseThrow(() -> new RuntimeException("Scope not found: " + "microprofile-jwt"));     //

        keycloak.realms().realm(REALM).removeDefaultOptionalClientScope(microprofileScopeId);
        keycloak.realms().realm(REALM).addDefaultDefaultClientScope(microprofileScopeId);
    }*/

    private void configureClient() {
        RealmResource realmResource = keycloak.realm(REALM);
        ClientsResource clientsResource = realmResource.clients();
//        ClientRepresentation existingClient = findClientByClientId(clientsResource);
        ClientRepresentation clientRepresentation = getClientRepresentation();
        realmResource.clients().create(clientRepresentation);
//        try (Response response = realmResource.clients().create(clientRepresentation)) {
//            if (Response.Status.CREATED.getStatusCode() != response.getStatus())
//                throw new RuntimeException("Error occurred with code " + response.getStatus() + " and message: " + response);
//        }
    }

    private ClientRepresentation getClientRepresentation() {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(CLIENT);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setDefaultClientScopes(List.of("web-origins", "acr", "profile", "roles", "user-profile-attributes", "microprofile-jwt", "basic", "email"));
        clientRepresentation.setOptionalClientScopes(List.of("address", "phone", "organization", "offline_access"));
        clientRepresentation.setRedirectUris(List.of("*"));
        clientRepresentation.setWebOrigins(List.of("*"));
        clientRepresentation.setAttributes(Map.of("post.logout.redirect.uris", "+"));
        return clientRepresentation;
    }

    // set to private
    public boolean realmExists() {
        try {
            keycloak.realm(REALM).toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //set to private
    public ClientRepresentation findClientByClientId(ClientsResource clientsResource) {
        try {
            List<ClientRepresentation> clients = clientsResource.findByClientId(CLIENT);
            return clients.isEmpty() ? null : clients.getFirst();
        } catch (NotFoundException e) {
            return null;
        }
    }
}