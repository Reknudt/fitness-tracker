package com.pavlov.media.serviceKeycloak.service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CONFLICT;

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
        keycloak.realms().realm(REALM).update(realmRepresentation);
        configureClient();
    }

    private void setRealmScopeMicroproflieToDefault() {
        List<ClientScopeRepresentation> optionalClientScopeRepresentations = keycloak.realms().realm(REALM).getDefaultOptionalClientScopes();

        String microprofileScopeId = optionalClientScopeRepresentations.stream()
                .filter(s -> "microprofile-jwt".equals(s.getName()))
                .findFirst()
                .map(ClientScopeRepresentation::getId)
                .orElseThrow(() -> new RuntimeException("Scope not found: " + "microprofile-jwt"));     //

        keycloak.realms().realm(REALM).removeDefaultOptionalClientScope(microprofileScopeId);
        keycloak.realms().realm(REALM).addDefaultDefaultClientScope(microprofileScopeId);
    }

    private void configureClient() {
        RealmResource realmResource = keycloak.realm(REALM);
        ClientsResource clientsResource = realmResource.clients();
        ClientRepresentation existingClient = findClientByClientId(clientsResource);
        if (existingClient != null)
            throw new ResponseStatusException(CONFLICT, "Client already set up");
        ClientRepresentation clientRepresentation = getClientRepresentation();
        try (Response response = realmResource.clients().create(clientRepresentation)) {
//            response;
        }
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

//    public List<String> getRealmDefaultScopeIds(String realmName) {
//        RealmResource realm = keycloak.realm(realmName);
//        RealmRepresentation realmRep = realm.toRepresentation();
//        return realmRep.getDefaultDefaultClientScopes();
//    }

    /*public void addScopeToRealmDefaults(String realmName, String scopeName) {
        RealmResource realm = keycloak.realm(realmName);

        RealmRepresentation realmRep = realm.toRepresentation();
        List<String> currentDefaultIds = realmRep.getDefaultDefaultClientScopes();

        if (currentDefaultIds == null) {
            currentDefaultIds = new ArrayList<>();
        }

        // Добавляем ID нового scope'а
        String scopeId = getScopeId(realmName, scopeName);
        if (!currentDefaultIds.contains(scopeId)) {
            currentDefaultIds.add(scopeId);
            realmRep.setDefaultDefaultClientScopes(currentDefaultIds);
            realm.update(realmRep);
            System.out.println("Added " + scopeName + " to realm default scopes via RealmRepresentation");
        }
    }

    private String getScopeId(String realmName, String scopeName) {
        RealmResource realm = keycloak.realm(realmName);
        return realm.clientScopes().findAll().stream()
                .filter(s -> scopeName.equals(s.getName()))
                .findFirst()
                .map(ClientScopeRepresentation::getId)
                .orElseThrow(() -> new RuntimeException("Scope not found: " + scopeName));
    }*/

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