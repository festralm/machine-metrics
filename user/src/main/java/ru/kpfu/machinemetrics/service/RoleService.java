package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import ru.kpfu.machinemetrics.properties.KeycloakProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    public static final Map<String, String> ALLOWED_ROLES = Map.of(
            "ADMIN", "Администратор",
            "MODERATOR", "Модератор",
            "USER", "Пользователь"
    );

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;


    public List<RoleRepresentation> findAll() {
        return keycloak
                .realm(keycloakProperties.getRealm())
                .roles()
                .list()
                .stream()
                .filter(x -> ALLOWED_ROLES.containsKey(x.getName()))
                .peek(x -> x.setDescription(ALLOWED_ROLES.get(x.getName())))
                .collect(Collectors.toList());
    }

    public RoleRepresentation findByName(String roleName) {
        return keycloak
                .realm(keycloakProperties.getRealm())
                .roles()
                .get(roleName)
                .toRepresentation();
    }
}
