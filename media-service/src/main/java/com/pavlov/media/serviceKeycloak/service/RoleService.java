package com.pavlov.media.serviceKeycloak.service;

import com.pavlov.media.serviceKeycloak.PredefinedRolesConfig;
import com.pavlov.media.serviceKeycloak.request.RoleRequest;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RolesResource rolesResource;
    private final PredefinedRolesConfig rolesConfig;

    public List<PredefinedRolesConfig.RoleConfig> getPredefinedRoleConfigs() {
        return rolesConfig.getRoles();
    }

    public List<PredefinedRolesConfig.CompositeRoleConfig> getPredefinedCompositeRoleConfigs() {
        return rolesConfig.getCompositeRoles();
    }

    public void createMissingPredefinedRoles() {
        for (PredefinedRolesConfig.RoleConfig roleConfig : rolesConfig.getRoles()) {
            if (!roleExists(roleConfig.getName())) {
                RoleRequest request = new RoleRequest();
                request.setName(roleConfig.getName());
                request.setDescription(roleConfig.getDescription());
                request.setComposite(false);
                createRole(request);
                log.info("Created missing predefined role: {}", roleConfig.getName());
            }
        }
        for (PredefinedRolesConfig.CompositeRoleConfig compositeConfig : rolesConfig.getCompositeRoles()) {
            if (!roleExists(compositeConfig.getName())) {
                RoleRequest request = new RoleRequest();
                request.setName(compositeConfig.getName());
                request.setDescription(compositeConfig.getDescription());
                request.setComposite(true);
                request.setCompositeRoles(compositeConfig.getIncludedRoles());
                createRole(request);
                log.info("Created missing composite role: {}", compositeConfig.getName());
            }
        }
    }

//    // Метод для получения предопределённых ролей по категории
//    public List<RoleRepresentation> getPredefinedRolesByCategory(String category) {
//        // Можно добавить логику фильтрации по категориям
//        // Например, по префиксам: DOCUMENT_, MATERIALS_, DIRECTORY_, etc.
//        return rolesConfig.getRoles().stream()
//                .filter(role -> role.getName().startsWith(category.toUpperCase() + "_"))
//                .map(role -> {
//                    try {
//                        return rolesResource.get(role.getName()).toRepresentation();
//                    } catch (NotFoundException e) {
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }

    public void createPredefinedRoles() {
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
    }

    // ---------------------------

    public List<RoleRepresentation> getAllRoles() {
        return rolesResource.list();
    }

    public RoleRepresentation getRoleByName(String roleName) {
        try {
            RoleResource roleResource = rolesResource.get(roleName);
            return roleResource.toRepresentation();
        } catch (NotFoundException e) {
            log.warn("Role not found: {}", roleName);
            throw new ResponseStatusException(NOT_FOUND, "Role not found: " + roleName);
        }
    }

    public void createRole(RoleRequest request) {
        if (roleExists(request.getName()))
            throw new ResponseStatusException(CONFLICT, "Role already exists: " + request.getName());
        RoleRepresentation newRole = new RoleRepresentation();
        newRole.setName(request.getName());
        newRole.setDescription(request.getDescription());
        newRole.setComposite(request.isComposite());
        if (request.getAttributes() != null)
            newRole.setAttributes(request.getAttributes());
        rolesResource.create(newRole);
        if (request.isComposite() && request.getCompositeRoles() != null && !request.getCompositeRoles().isEmpty())
            addCompositeRoles(request.getName(), request.getCompositeRoles());
    }

    public void updateRole(String roleName, RoleRequest request) {
        try {
            RoleResource roleResource = rolesResource.get(roleName);
            RoleRepresentation role = roleResource.toRepresentation();
            if (request.getDescription() != null)
                role.setDescription(request.getDescription());
            if (request.getAttributes() != null)
                role.setAttributes(request.getAttributes());
            roleResource.update(role);

            // Обновляем композитные роли
            if (request.getCompositeRoles() != null)
                updateCompositeRoles(roleName, request.getCompositeRoles());
        } catch (NotFoundException e) {
            log.warn("Role not found for update: {}", roleName);
            throw new ResponseStatusException(NOT_FOUND, "Role not found: " + roleName);
        }
    }

    public void deleteRole(String roleName) {
        try {
            rolesResource.deleteRole(roleName);
        } catch (NotFoundException e) {
            log.warn("Role not found for deletion: {}", roleName);
            throw new ResponseStatusException(NOT_FOUND, "Role not found: " + roleName);
        }
    }

    public boolean roleExists(String roleName) {
        try {
            rolesResource.get(roleName).toRepresentation();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private void addCompositeRoles(String roleName, List<String> compositeRoleNames) {
        RoleResource roleResource = rolesResource.get(roleName);
        List<RoleRepresentation> compositeRoles = compositeRoleNames.stream().map(name -> rolesResource.get(name).toRepresentation()).collect(Collectors.toList());
        roleResource.addComposites(compositeRoles);
    }

    private void updateCompositeRoles(String roleName, List<String> newCompositeRoles) {
        RoleResource roleResource = rolesResource.get(roleName);
        Set<RoleRepresentation> currentComposites = roleResource.getRoleComposites();   // Получаем текущие композитные роли
        if (!currentComposites.isEmpty())                                               // Удаляем старые композитные роли
            roleResource.deleteComposites(currentComposites.stream().toList());
        if (!newCompositeRoles.isEmpty()) {                                             // Добавляем новые композитные роли
            List<RoleRepresentation> compositeRoles = newCompositeRoles.stream().map(name -> rolesResource.get(name).toRepresentation()).collect(Collectors.toList());
            roleResource.addComposites(compositeRoles);
        }
    }
}
