package com.pavlov.media.serviceKeycloak.request;

import lombok.Getter;
import lombok.Setter;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserRequest {
    private String id;
    private String username;
    private String email;
//    private String firstName;
//    private String lastName;
    private boolean enabled = true;
    private Map<String, List<String>> attributes;   // fullname
    private List<String> requiredActions;
    private List<RoleRepresentation> roles;
}