package com.pavlov.media.serviceKeycloak.service;

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
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RolesResource rolesResource;

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
