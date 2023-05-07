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
import ru.kpfu.machinemetrics.model.User;
import ru.kpfu.machinemetrics.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@SpringBootTest
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UserServiceTest {

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private UserService userService;

    @Test
    public void testGetAllNotDeleted() {
        // given
        User user1 = User.builder()
                .name("User 1")
                .build();
        User user2 = User.builder()
                .name("User 2")
                .build();
        List<User> userList = List.of(user1, user2);

        when(userRepositoryMock.findAllByDeletedFalse()).thenReturn(userList);

        // when
        List<User> result = userService.getAllNotDeleted();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getName()).isEqualTo(user1.getName());
        softly.assertThat(result.get(1).getName()).isEqualTo(user2.getName());
        softly.assertAll();
    }

    @Test
    void testSave() {
        // given
        User user = User.builder()
                .name("Test User")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name(user.getName())
                .build();

        when(userRepositoryMock.save(any(User.class))).thenReturn(savedUser);

        // when
        User actualUser = userService.save(user);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getId()).isEqualTo(savedUser.getId());
        softly.assertThat(actualUser.getName()).isEqualTo(savedUser.getName());
        softly.assertThat(actualUser.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testGetByIdFound() {
        // given
        User user = new User();
        user.setId(1L);
        user.setName("User 1");

        when(userRepositoryMock.findByIdAndDeletedFalse(user.getId())).thenReturn(Optional.of(user));

        // when
        User actualUser = userService.getById(user.getId());

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
        softly.assertThat(actualUser.getName()).isEqualTo(user.getName());
        softly.assertThat(actualUser.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testGetByIdNotFound() {
        // given
        Long givenId = 1L;

        when(userRepositoryMock.findByIdAndDeletedFalse(givenId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.getById(givenId));

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
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setDeleted(false);

        when(userRepositoryMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        // when
        userService.delete(userId);

        // then
        verify(userRepositoryMock, Mockito.times(1)).findByIdAndDeletedFalse(userId);
        verify(userRepositoryMock, Mockito.times(1)).save(user);
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    void testDeleteWithNonExistingUser() {
        // given
        Long userId = 1L;
        when(userRepositoryMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.delete(userId));

        // then
        verify(userRepositoryMock, Mockito.times(1)).findByIdAndDeletedFalse(userId);
        verify(userRepositoryMock, Mockito.never()).save(Mockito.any(User.class));
        String expectedMessage = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testEdit() {
        // given
        Long userId = 1L;

        User existingUser = User.builder()
                .id(userId)
                .name("Existing User")
                .build();

        User updatedUser = User.builder()
                .name("Updated User")
                .build();

        when(userRepositoryMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(existingUser));
        when(userRepositoryMock.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User actualUser = userService.edit(userId, updatedUser);

        // then
        verify(userRepositoryMock).findByIdAndDeletedFalse(userId);
        verify(userRepositoryMock).save(existingUser);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualUser.getId()).isEqualTo(existingUser.getId());
        softly.assertThat(actualUser.getName()).isEqualTo(updatedUser.getName());
        softly.assertAll();
    }

    @Test
    void testEditUserNotFound() {
        // given
        Long userId = 1L;

        when(userRepositoryMock.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.delete(userId));

        // then
        String expectedMessage = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                new Locale("ru")
        );
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);
    }
}
