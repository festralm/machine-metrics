package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.AuthUser;
import ru.kpfu.machinemetrics.repository.AuthUserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.AuthUserConstants.AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class AuthUserServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private AuthUserRepository authUserRepositoryMock;

    @Autowired
    private AuthUserService authUserService;

    @Test
    public void testGetAll() {
        // given
        AuthUser user1 = AuthUser.builder()
                .id(1L)
                .email("Email 1")
                .password("Password 1")
                .build();
        AuthUser user2 = AuthUser.builder()
                .id(2L)
                .email("Email 2")
                .password("Password 2")
                .build();
        List<AuthUser> userList = List.of(user1, user2);

        when(authUserRepositoryMock.findAll()).thenReturn(userList);

        // when
        List<AuthUser> result = authUserService.getAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getId()).isEqualTo(user1.getId());
        softly.assertThat(result.get(0).getEmail()).isEqualTo(user1.getEmail());
        softly.assertThat(result.get(0).getPassword()).isEqualTo(user1.getPassword());
        softly.assertThat(result.get(1).getId()).isEqualTo(user2.getId());
        softly.assertThat(result.get(1).getEmail()).isEqualTo(user2.getEmail());
        softly.assertThat(result.get(1).getPassword()).isEqualTo(user2.getPassword());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        AuthUser user = AuthUser.builder()
                .id(1L)
                .email("Email 1")
                .password("Password 1")
                .build();

        when(authUserRepositoryMock.save(any(AuthUser.class))).thenReturn(user);

        // when
        AuthUser actualUser = authUserService.save(user);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
        softly.assertThat(actualUser.getEmail()).isEqualTo(user.getEmail());
        softly.assertThat(actualUser.getPassword()).isEqualTo(user.getPassword());
        softly.assertAll();
    }

    @Test
    public void testGetByIdFound() {
        // given
        AuthUser user = AuthUser.builder()
                .id(1L)
                .email("Email 1")
                .password("Password 1")
                .build();

        when(authUserRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        AuthUser actualUser = authUserService.getById(user.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
        softly.assertThat(actualUser.getEmail()).isEqualTo(user.getEmail());
        softly.assertThat(actualUser.getPassword()).isEqualTo(user.getPassword());
        softly.assertAll();
    }

    @Test
    public void testGetByIdNotFound() {
        // given
        Long givenId = 1L;

        when(authUserRepositoryMock.findById(givenId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authUserService.getById(givenId));

        // then
        String expectedMessage = messageSource.getMessage(
                AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{givenId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testDeleteWithExistingUser() {
        // given
        Long userId = 1L;

        AuthUser user = AuthUser.builder()
                .id(userId)
                .email("Email 1")
                .password("Password 1")
                .build();

        when(authUserRepositoryMock.findById(userId)).thenReturn(Optional.of(user));

        // when
        authUserService.delete(userId);

        // then
        verify(authUserRepositoryMock, Mockito.times(1)).findById(userId);
        verify(authUserRepositoryMock, Mockito.times(1)).delete(user);
    }

    @Test
    void testDeleteWithNonExistingUser() {
        // given
        Long userId = 1L;
        when(authUserRepositoryMock.findById(userId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authUserService.delete(userId));

        // then
        verify(authUserRepositoryMock, Mockito.times(1)).findById(userId);
        verify(authUserRepositoryMock, Mockito.never()).delete(Mockito.any(AuthUser.class));
        String expectedMessage = messageSource.getMessage(
                AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
