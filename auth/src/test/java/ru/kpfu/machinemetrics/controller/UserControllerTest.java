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
import ru.kpfu.machinemetrics.exception.ResourceNotFoundException;
import ru.kpfu.machinemetrics.model.AuthUser;
import ru.kpfu.machinemetrics.service.AuthUserService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kpfu.machinemetrics.constants.AuthUserConstants.AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE;

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
    private AuthUserService authUserServiceMock;

    @Test
    public void testListAll() throws Exception {
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

        List<AuthUser> expectedUserList = List.of(user1, user2);

        when(authUserServiceMock.getAll()).thenReturn(userList);

        // expect
        mockMvc.perform(get("/api/v1/auth-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$[0].password").value(user1.getPassword()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()))
                .andExpect(jsonPath("$[1].email").value(user1.getEmail()))
                .andExpect(jsonPath("$[1].password").value(user1.getPassword()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    List<AuthUser> actualList = Arrays.asList(objectMapper.readValue(response,
                            AuthUser[].class));

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualList).isNotNull();
                    softly.assertThat(actualList).hasSize(2);
                    softly.assertThat(actualList.get(0).getId()).isEqualTo(user1.getId());
                    softly.assertThat(actualList.get(0).getEmail()).isEqualTo(user1.getEmail());
                    softly.assertThat(actualList.get(0).getPassword()).isEqualTo(user1.getPassword());
                    softly.assertThat(actualList.get(1).getId()).isEqualTo(user1.getId());
                    softly.assertThat(actualList.get(1).getEmail()).isEqualTo(user1.getEmail());
                    softly.assertThat(actualList.get(1).getPassword()).isEqualTo(user1.getPassword());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreate() throws Exception {
        // given

        AuthUser user = AuthUser.builder()
                .id(1L)
                .email("Email 1")
                .password("Password 1")
                .build();

        when(authUserServiceMock.save(eq(user))).thenReturn(user);

        // expect
        mockMvc.perform(post("/api/v1/auth-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").value(user.getPassword()))
                .andExpect(header().string("Location", "/user/" + user.getId()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    AuthUser actualUser = objectMapper.readValue(response, AuthUser.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUser).isNotNull();
                    softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
                    softly.assertThat(actualUser.getEmail()).isEqualTo(user.getEmail());
                    softly.assertThat(actualUser.getPassword()).isEqualTo(user.getPassword());
                    softly.assertAll();
                });
    }

    @Test
    public void testCreateWithEmptyId() throws Exception {
        // given
        AuthUser userCreateDto = AuthUser.builder()
                .email("not empty")
                .password("Password 1")
                .build();

        String message = messageSource.getMessage("validation.user.id.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/auth-user")
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
    public void testCreateWithEmptyEmail() throws Exception {
        // given
        AuthUser userCreateDto = AuthUser.builder()
                .id(1L)
                .email("")
                .password("Password 1")
                .build();

        String message = messageSource.getMessage("validation.user.email.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/auth-user")
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
    public void testCreateWithEmptyPassword() throws Exception {
        // given
        AuthUser userCreateDto = AuthUser.builder()
                .id(1L)
                .email("not empty")
                .build();

        String message = messageSource.getMessage("validation.user.password.empty", null, new Locale("ru"));
        ErrorResponse expectedResponseBody = new ErrorResponse(400, "\"" + message + "\"");

        // expect
        mockMvc.perform(post("/api/v1/auth-user")
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
        AuthUser user = AuthUser.builder()
                .id(1L)
                .email("Email 1")
                .password("Password 1")
                .build();

        when(authUserServiceMock.getById(user.getId())).thenReturn(user);

        // expect
        mockMvc.perform(get("/api/v1/auth-user/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").value(user.getPassword()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    AuthUser actualUser = objectMapper.readValue(response, AuthUser.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUser).isNotNull();
                    softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
                    softly.assertThat(actualUser.getEmail()).isEqualTo(user.getEmail());
                    softly.assertThat(actualUser.getPassword()).isEqualTo(user.getPassword());
                    softly.assertAll();
                });
    }

    @Test
    public void testGetNonExistingUser() throws Exception {
        // given
        Long userId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                locale
        );

        when(authUserServiceMock.getById(userId)).thenThrow(new ResourceNotFoundException(message));

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(get("/api/v1/auth-user/{id}", userId))
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
        doNothing().when(authUserServiceMock).delete(userId);

        // expect
        mockMvc.perform(delete("/api/v1/auth-user/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNonExistingUser() throws Exception {
        // given
        Long userId = 1L;

        Locale locale = new Locale("ru");
        String message = messageSource.getMessage(
                AUTH_USER_NOT_FOUND_EXCEPTION_MESSAGE,
                new Object[]{userId},
                locale
        );

        doThrow(new ResourceNotFoundException(message)).when(authUserServiceMock).delete(userId);

        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);

        // expect
        mockMvc.perform(delete("/api/v1/auth-user/{id}", userId))
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
