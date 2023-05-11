package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.exception.ResourceNotCreatedException;
import ru.kpfu.machinemetrics.exception.ResourceNotDeletedException;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.exception.ValidationException;
import ru.kpfu.machinemetrics.properties.KeycloakProperties;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_CREATED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_DELETED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_PASSWORD_EMPTY_VALIDATION_MESSAGE;
import static ru.kpfu.machinemetrics.service.RoleService.ALLOWED_ROLES;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Keycloak keycloak;
    private final RoleService roleService;
    private final KeycloakProperties keycloakProperties;
    private final MessageSource messageSource;

    public List<UserRepresentation> findAll(String except) {
        return keycloak
                .realm(keycloakProperties.getRealm())
                .users()
                .list()
                .stream()
                .filter(x -> !x.getId().equals(except))
                .collect(Collectors.toList());
    }

    public UserRepresentation create(UserCreateDto dto) {
        if (!StringUtils.hasText(dto.getPassword()) || !StringUtils.hasText(dto.getEmail())) {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(
                    USER_PASSWORD_EMPTY_VALIDATION_MESSAGE,
                    null,
                    locale
            );
            throw new ValidationException(message);
        }
        var user = prepareUserRepresentation(dto);
        try (Response response = keycloak
                .realm(keycloakProperties.getRealm())
                .users()
                .create(user)) {
            if (response == null || response.getStatus() != 201) {
                throw createNotCreatedException(USER_NOT_CREATED_EXCEPTION_MESSAGE);
            }
            String locationHeader = response.getLocation().toString();
            final String id = extractUserIdFromLocationHeader(locationHeader);
            assignRole(id, dto.getRoleName());
            return findById(id);
        } catch (Exception e) {
            throw createNotCreatedException(USER_NOT_CREATED_EXCEPTION_MESSAGE);
        }
    }

    public UserRepresentation edit(String id, UserCreateDto dto) {
        try {
            UserResource userResource = keycloak
                    .realm(keycloakProperties.getRealm())
                    .users()
                    .get(id);
            var user = prepareUserRepresentation(userResource.toRepresentation(), dto);
            userResource.update(user);
            assignRole(id, dto.getRoleName());
            return findById(id);
        } catch (NotFoundException e) {
            throw createNotFoundException(id);
        }
    }

    private String extractUserIdFromLocationHeader(String locationHeader) {
        return locationHeader.replace(
                keycloakProperties.getAuthServerUrl() + "/admin/realms/master/users/",
                ""
        );
    }

    public UserRepresentation findById(String id) {
        try {
            final UserResource userResource = keycloak
                    .realm(keycloakProperties.getRealm())
                    .users()
                    .get(id);
            final UserRepresentation representation = userResource
                    .toRepresentation();
            representation.setRealmRoles(
                    userResource
                            .roles()
                            .realmLevel()
                            .listAll()
                            .stream()
                            .map(RoleRepresentation::getName)
                            .filter(ALLOWED_ROLES::containsKey)
                            .map(ALLOWED_ROLES::get)
                            .collect(Collectors.toList())
            );
            return representation;
        } catch (NotFoundException e) {
            throw createNotFoundException(id);
        }
    }

    private RuntimeException createNotFoundException(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{id},
                locale
        );
        return new ResourceNotFoundException(message);
    }

    public void assignRole(String id, String roleName) {
        var role = roleService.findByName(roleName);
        if (role == null) {
            throw createNotFoundException(roleName);
        }
        final RoleScopeResource realmLevel = keycloak
                .realm(keycloakProperties.getRealm())
                .users()
                .get(id)
                .roles()
                .realmLevel();
        realmLevel
                .remove(realmLevel.listAll());
        realmLevel.add(List.of(role));
    }

    private RuntimeException createNotCreatedException(String messageCode) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                messageCode,
                null,
                locale
        );
        return new ResourceNotCreatedException(message);
    }

    private RuntimeException createNotDeletedException(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(
                USER_NOT_DELETED_EXCEPTION_MESSAGE,
                new Object[]{id},
                locale
        );
        return new ResourceNotDeletedException(message);
    }

    public void delete(String id) {
        final Response response = keycloak
                .realm(keycloakProperties.getRealm())
                .users()
                .delete(id);
        if (response.getStatus() == 404) {
            throw createNotFoundException(id);
        }
        if (response.getStatus() != 204) {
            throw createNotDeletedException(id);
        }
    }

    private CredentialRepresentation preparePasswordRepresentation(String password) {
        var cR = new CredentialRepresentation();
        cR.setTemporary(false);
        cR.setType(CredentialRepresentation.PASSWORD);
        cR.setValue(password);
        return cR;
    }

    private UserRepresentation prepareUserRepresentation(UserCreateDto dto) {
        var newUser = new UserRepresentation();
        return prepareUserRepresentation(newUser, dto);
    }

    private UserRepresentation prepareUserRepresentation(UserRepresentation user, UserCreateDto dto) {
        if (dto.getPassword() != null) {
            var cR = preparePasswordRepresentation(dto.getPassword());
            user.setCredentials(List.of(cR));
        }
        if (dto.getEmail() != null) {
            user.setUsername(dto.getEmail());
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }
}
