package com.pavlov.media.serviceKeycloak;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "keycloak.predefined-roles")
@Getter
@Setter
@RequiredArgsConstructor
@Validated
public class PredefinedRolesConfig {
    
    private List<RoleConfig> roles = new ArrayList<>();
    private List<CompositeRoleConfig> compositeRoles = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleConfig {
        @NotBlank
        private String name;
        
        @NotBlank
        private String description;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompositeRoleConfig {
        @NotBlank
        private String name;
        
        @NotBlank
        private String description;
        
        @NotEmpty
        private List<String> includedRoles;
    }
}