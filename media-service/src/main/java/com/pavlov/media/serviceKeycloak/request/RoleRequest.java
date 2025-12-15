package com.pavlov.media.serviceKeycloak.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RoleRequest {
//    @NotNull
    private String name;
    private String description;
    private boolean composite;
    private Map<String, List<String>> attributes;   // add attribute isPredefined
    private List<String> compositeRoles;
}