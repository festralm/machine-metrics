package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.constants.UserConstants;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.exception.ResourceNotCreatedException;
import ru.kpfu.machinemetrics.exception.ResourceNotDeletedException;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_DELETED_EXCEPTION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest({"keycloak.realm=realm", "keycloak.auth-server-url=http://localhost:8080"})
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UserServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private Keycloak keycloakMock;

    @MockBean
    private RoleService roleServiceMock;

    @Autowired
    private UserService userService;

    @Test
    public void findAll() {
        // given
        UserRepresentation user1 = new UserRepresentation();
        user1.setUsername("User 1");
        user1.setId("id1");

        UserRepresentation user2 = new UserRepresentation();
        user2.setUsername("User 2");
        user2.setId("id2");

        UserRepresentation user3 = new UserRepresentation();
        user3.setUsername("User 3");
        user3.setId("id3");

        List<UserRepresentation> userList = List.of(user1, user2);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.list()).thenReturn(userList);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        List<UserRepresentation> result = userService.findAll("id3");

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getUsername()).isEqualTo(user1.getUsername());
        softly.assertThat(result.get(1).getUsername()).isEqualTo(user2.getUsername());
        softly.assertAll();
    }

    @Test
    void testSave() throws URISyntaxException {
        // given
        UserCreateDto dto = UserCreateDto.builder()
                .firstName("First Name")
                .lastName("Last Name")
                .email("email@email.com")
                .password("password")
                .roleName("admin")
                .build();

        RoleRepresentation roleRepresentationMock = mock(RoleRepresentation.class);

        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(201);
        when(responseMock.getLocation()).thenReturn(new URI("http://localhost:8080/admin/realms/master/users/test"));

        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);
        doNothing().when(roleScopeResourceMock).add(any());

        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        UserRepresentation user = new UserRepresentation();
        user.setUsername("User 1");
        user.setId("id1");

        UserResource userResourceMock = mock(UserResource.class);
        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(userResourceMock.toRepresentation()).thenReturn(user);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.create(any())).thenReturn(responseMock);
        when(usersResourceMock.get("test")).thenReturn(userResourceMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);
        when(roleServiceMock.findByName("admin")).thenReturn(roleRepresentationMock);

        // when
        UserRepresentation response = userService.create(dto);

        // then
        verify(roleScopeResourceMock, times(1)).add(any());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void testSaveWhenNotSaved() {
        // given
        UserCreateDto dto = UserCreateDto.builder()
                .firstName("First Name")
                .lastName("Last Name")
                .email("email@email.com")
                .password("password")
                .roleName("admin")
                .build();

        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(500);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.create(any())).thenReturn(responseMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        Throwable thrown = catchThrowable(() -> userService.create(dto));

        // then
        String expectedMessage = messageSource.getMessage(
                UserConstants.USER_NOT_CREATED_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotCreatedException.class).hasMessage(expectedMessage);
    }

    @Test
    void testSaveWhenUsersResourceThrowsException() {
        // given
        UserCreateDto dto = UserCreateDto.builder().email("email@email.com").password("password").build();

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.create(any())).thenThrow(new RuntimeException());

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        Throwable thrown = catchThrowable(() -> userService.create(dto));

        // then
        String expectedMessage = messageSource.getMessage(
                UserConstants.USER_NOT_CREATED_EXCEPTION_MESSAGE,
                null,
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotCreatedException.class).hasMessage(expectedMessage);
    }

    @Test
    public void testFetByIdFound() {
        // given
        UserRepresentation user = new UserRepresentation();
        user.setUsername("User 1");

        RoleRepresentation role = new RoleRepresentation();
        role.setName("ADMIN");

        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(role));

        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        UserResource userResourceMock = mock(UserResource.class);
        when(userResourceMock.toRepresentation()).thenReturn(user);
        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.get(user.getId())).thenReturn(userResourceMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        UserRepresentation actualUser = userService.findById(user.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getUsername()).isEqualTo(user.getUsername());
        softly.assertThat(actualUser.getRealmRoles()).containsExactly("Администратор");
        softly.assertAll();
    }

    @Test
    public void testFetByIdNotFound() {
        // given
        String givenId = "id";

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.get(givenId)).thenThrow(new NotFoundException());

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        Throwable thrown = catchThrowable(() -> userService.findById(givenId));

        // then
        String expectedMessage = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingUser() {
        // given
        String givenId = "id";

        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(204);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.delete(givenId)).thenReturn(responseMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        userService.delete(givenId);
    }

    @Test
    void testDeleteWithNonExistingUser() {
        // given
        String givenId = "id";

        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(404);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.delete(givenId)).thenReturn(responseMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        Throwable thrown = catchThrowable(() -> userService.delete(givenId));
        String expectedMessage = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWhenNotDeleted() {
        // given
        String givenId = "id";

        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(500);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(usersResourceMock.delete(givenId)).thenReturn(responseMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        Throwable thrown = catchThrowable(() -> userService.delete(givenId));
        String expectedMessage = messageSource.getMessage(
                USER_NOT_DELETED_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotDeletedException.class).hasMessage(expectedMessage);
    }

    // todo

//    @Test
//    void testEdit() {
//        // given
//        Long userId = 1L;
//
//        User existingUser = User.builder()
//                .id(userId)
//                .name("Existing User")
//                .build();
//
//        User updatedUser = User.builder()
//                .name("Updated User")
//                .build();
//
//        when(keycloakMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));
//        when(keycloakMock.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        User actualUser = userService.edit(userId, updatedUser);
//
//        // then
//        verify(keycloakMock).findByIdAndDeletedFalse(userId);
//        verify(keycloakMock).save(existingUser);
//
//        SoftAssertions softly = new SoftAssertions();
//        softly.assertThat(actualUser.getId()).isEqualTo(existingUser.getId());
//        softly.assertThat(actualUser.getName()).isEqualTo(updatedUser.getName());
//        softly.assertAll();
//    }
//
//    @Test
//    void testEditUserNotFound() {
//        // given
//        Long userId = 1L;
//
//        when(keycloakMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());
//
//        // when
//        Throwable thrown = catchThrowable(() -> userService.delete(userId));
//
//        // then
//        String expectedMessage = messageSource.getMessage(
//                USER_NOT_FOUND_EXCEPTION_MESSAGE,
//                new Object[]{userId},
//                new Locale("ru")
//        );
//        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
//                .hasMessage(expectedMessage);
//    }
}
