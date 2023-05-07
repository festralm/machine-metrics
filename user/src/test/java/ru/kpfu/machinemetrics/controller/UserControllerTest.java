package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.configuration.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.dto.UserDetailsDto;
import ru.kpfu.machinemetrics.dto.UserItemDto;
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.mapper.UserMapper;
import ru.kpfu.machinemetrics.model.User;
import ru.kpfu.machinemetrics.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@WebMvcTest(UserController.class)
@ImportAutoConfiguration(MessageSourceConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private UserMapper userMapperMock;

    @Test
    public void testListAll() throws Exception {
        // given
        User user1 = User.builder()
                .id(0L)
                .name("User 1")
                .build();
        User user2 = User.builder()
                .id(1L)
                .name("User 2")
                .build();
        List<User> userList = List.of(user1, user2);

        UserItemDto userItemDto1 = UserItemDto.builder()
                .id(user1.getId())
                .name(user1.getName())
                .build();
        UserItemDto userCreateDto2 = UserItemDto.builder()
                .id(user2.getId())
                .name(user2.getName())
                .build();
        List<UserItemDto> expectedUserItemDtoList = List.of(userItemDto1, userCreateDto2);

        when(userServiceMock.getAllNotDeleted()).thenReturn(userList);
        when(userMapperMock.toUserItemDtos(eq(userList))).thenReturn(expectedUserItemDtoList);

        // expect
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[0].name").value(user1.getName()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()))
                .andExpect(jsonPath("$[1].name").value(user2.getName()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<UserItemDto> actualList = Arrays.asList(objectMapper.readValue(response,
                            UserItemDto[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getName()).isEqualTo(user1.getName());
                    softly.assertThat(actualList.get(1).getName()).isEqualTo(user2.getName());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("User 1")
                .surname("UserS 1")
                .build();

        User user = User.builder()
                .name(userCreateDto.getName())
                .surname(userCreateDto.getSurname())
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name(user.getName())
                .surname(user.getSurname())
                .build();

        UserDetailsDto savedUserDetailsDto = UserDetailsDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .surname(savedUser.getSurname())
                .build();

        when(userMapperMock.toUser(any(UserCreateDto.class))).thenReturn(user);
        when(userServiceMock.save(eq(user))).thenReturn(savedUser);
        when(userMapperMock.toUserDetailsDto(eq(savedUser))).thenReturn(savedUserDetailsDto);

        // expect
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedUserDetailsDto.getId()))
                .andExpect(jsonPath("$.name").value(savedUserDetailsDto.getName()))
                .andExpect(jsonPath("$.surname").value(savedUserDetailsDto.getSurname()))
                .andExpect(header().string("Location", "/user/" + savedUserDetailsDto.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    UserDetailsDto actualUserCreateDto = objectMapper.readValue(response,
                            UserDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUserCreateDto).isNotNull();
                    softly.assertThat(actualUserCreateDto.getId()).isEqualTo(savedUserDetailsDto.getId());
                    softly.assertThat(actualUserCreateDto.getName()).isEqualTo(savedUserDetailsDto.getName());
                    softly.assertThat(actualUserCreateDto.getSurname()).isEqualTo(savedUserDetailsDto.getSurname());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("")
                .build();

        String message = messageSource.getMessage("validation.user.name.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {

                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse actualResponseBody = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualResponseBody).isNotNull();
                    softly.assertThat(actualResponseBody.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(actualResponseBody.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetExistingUser() throws Exception {
        // given
        User user = User.builder()
                .id(1L)
                .name("User 1")
                .surname("Users 1")
                .build();

        UserDetailsDto userDetailsDto = UserDetailsDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .build();

        when(userServiceMock.getById(user.getId())).thenReturn(user);
        when(userMapperMock.toUserDetailsDto(user)).thenReturn(userDetailsDto);

        // expect
        mockMvc.perform(get("/api/v1/user/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.surname").value(user.getSurname()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    UserDetailsDto actualUserDetailsDto = objectMapper.readValue(response,
                            UserDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUserDetailsDto).isNotNull();
                    softly.assertThat(actualUserDetailsDto.getId()).isEqualTo(user.getId());
                    softly.assertThat(actualUserDetailsDto.getName()).isEqualTo(user.getName());
                    softly.assertThat(actualUserDetailsDto.getSurname()).isEqualTo(user.getSurname());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetNonExistingUser() throws Exception {
        // given
        Long userId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                locale
        );

        when(userServiceMock.getById(userId)).thenThrow(new ResourceNotFoundException(message));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(get("/api/v1/user/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(errorResponse).isNotNull();
                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testDeleteExistingUser() throws Exception {
        // given
        Long userId = 1L;
        doNothing().when(userServiceMock).delete(userId);

        // expect
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingUser() throws Exception {
        // given
        Long userId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(userServiceMock).delete(userId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(errorResponse).isNotNull();
                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditExistingUser() throws Exception {
        // given
        Long userId = 1L;

        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("User 1")
                .surname("Users 1")
                .build();

        User updatedUser = User.builder()
                .name(userCreateDto.getName())
                .surname(userCreateDto.getSurname())
                .build();

        User editedUser = User.builder()
                .id(userId)
                .name(updatedUser.getName())
                .surname(updatedUser.getSurname())
                .build();

        UserDetailsDto editedUserDetailsDto = UserDetailsDto.builder()
                .id(editedUser.getId())
                .name(editedUser.getName())
                .surname(editedUser.getSurname())
                .build();

        when(userMapperMock.toUser(any(UserCreateDto.class))).thenReturn(updatedUser);
        when(userServiceMock.edit(eq(userId), eq(updatedUser))).thenReturn(editedUser);
        when(userMapperMock.toUserDetailsDto(eq(editedUser))).thenReturn(editedUserDetailsDto);

        // when
        mockMvc.perform(put("/api/v1/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(editedUserDetailsDto.getName()))
                .andExpect(jsonPath("$.surname").value(editedUserDetailsDto.getSurname()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    UserDetailsDto actualUserDetailsDto = objectMapper.readValue(response,
                            UserDetailsDto.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUserDetailsDto).isNotNull();
                    softly.assertThat(actualUserDetailsDto.getId()).isEqualTo(editedUserDetailsDto.getId());
                    softly.assertThat(actualUserDetailsDto.getName()).isEqualTo(editedUserDetailsDto.getName());
                    softly.assertThat(actualUserDetailsDto.getSurname()).isEqualTo(editedUserDetailsDto.getSurname());
                    softly.assertAll();
                });
    }

    @Test
    public void testEditNonExistingUser() throws Exception {
        // given
        Long userId = 1L;


        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name("User 1")
                .surname("Users 1")
                .build();

        User updatedUser = User.builder()
                .name(userCreateDto.getName())
                .surname(userCreateDto.getSurname())
                .build();

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                locale
        );

        when(userMapperMock.toUser(any(UserCreateDto.class))).thenReturn(updatedUser);
        doThrow(new ResourceNotFoundException(message)).when(userServiceMock).edit(eq(userId),
                eq(updatedUser));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(put("/api/v1/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(errorResponse).isNotNull();
                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
                    softly.assertAll();
                });
    }
}
