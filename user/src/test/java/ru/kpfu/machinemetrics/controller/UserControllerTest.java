package ru.kpfu.machinemetrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;
import ru.kpfu.machinemetrics.dto.ErrorResponse;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_FIRST_NAME_EMPTY_VALIDATION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_LAST_NAME_EMPTY_VALIDATION_MESSAGE;
import static ru.kpfu.machinemetrics.constants.UserConstants.USER_ROLE_EMPTY_VALIDATION_MESSAGE;

@WebMvcTest(UserController.class)
@ImportAutoConfiguration({MessageSourceConfig.class})
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userServiceMock;

//    @Test
//    public void testListAll() throws Exception {
//        // given
//        UserRepresentation user1 = new UserRepresentation();
//        user1.setUsername("User 1");
//        user1.setId("id1");
//
//        UserRepresentation user2 = new UserRepresentation();
//        user2.setUsername("User 2");
//        user2.setId("id2");
//
//        List<UserRepresentation> userList = List.of(user1, user2);
//
//
//        when(userServiceMock.findAll("id3")).thenReturn(userList);
//
//        // expect
//        mockMvc.perform(get("/api/v1/user"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].id").value(user1.getId()))
//                .andExpect(jsonPath("$[0].username").value(user1.getUsername()))
//                .andExpect(jsonPath("$[1].id").value(user2.getId()))
//                .andExpect(jsonPath("$[1].username").value(user2.getUsername()))
//                .andDo(result -> {
//                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
//                    List<UserRepresentation> actualList = Arrays.asList(objectMapper.readValue(response,
//                            UserRepresentation[].class));
//
//                    SoftAssertions softly = new SoftAssertions();
//                    softly.assertThat(actualList).isNotNull();
//                    softly.assertThat(actualList).hasSize(2);
//                    softly.assertThat(actualList.get(0).getUsername()).isEqualTo(user1.getUsername());
//                    softly.assertThat(actualList.get(1).getUsername()).isEqualTo(user2.getUsername());
//                    softly.assertAll();
//                });
//    }

    @Test
    public void testCreate() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .firstName("First Name")
                .lastName("Last Name")
                .email("email@email.com")
                .password("password")
                .roleName("admin")
                .build();

        String id = "id1";

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userCreateDto.getEmail());
        user.setId(id);

        when(userServiceMock.create(any(UserCreateDto.class))).thenReturn(user);

        // expect
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/user/" + id));
    }

    @Test
    public void testCreateWithEmptyFirstName() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .lastName("Last Name")
                .email("email@email.com")
                .password("password")
                .roleName("admin")
                .build();

        String message = messageSource.getMessage(USER_FIRST_NAME_EMPTY_VALIDATION_MESSAGE, null, new Locale("ru"));
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
    public void testCreateWithEmptyLastName() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .firstName("First Name")
                .lastName("")
                .email("email@email.com")
                .password("password")
                .roleName("admin")
                .build();

        String message = messageSource.getMessage(USER_LAST_NAME_EMPTY_VALIDATION_MESSAGE, null, new Locale("ru"));
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
    public void testCreateWithEmptyRole() throws Exception {
        // given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .firstName("First Name")
                .lastName("Last Name")
                .email("email@email.com")
                .password("password")
                .build();

        String message = messageSource.getMessage(USER_ROLE_EMPTY_VALIDATION_MESSAGE, null, new Locale("ru"));
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
        UserRepresentation user = new UserRepresentation();
        user.setId("id1");
        user.setUsername("User 1");

        when(userServiceMock.findById(user.getId())).thenReturn(user);

        // expect
        mockMvc.perform(get("/api/v1/user/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    UserRepresentation actualUser = objectMapper.readValue(response,
                            UserRepresentation.class);

                    SoftAssertions softly = new SoftAssertions();
                    softly.assertThat(actualUser).isNotNull();
                    softly.assertThat(actualUser.getId()).isEqualTo(user.getId());
                    softly.assertThat(actualUser.getUsername()).isEqualTo(user.getUsername());
                    softly.assertAll();
                });
    }

    @Test
    public void testDeleteExistingUser() throws Exception {
        // given
        String userId = "id1";
        doNothing().when(userServiceMock).delete(userId);

        // expect
        mockMvc.perform(delete("/api/v1/user/{id}", userId))
                .andExpect(status().isNoContent());
    }

//    @Test
//    public void testEditExistingUser() throws Exception {
//        // given
//        Long userId = 1L;
//
//        UserCreateDto userCreateDto = UserCreateDto.builder()
//                .name("User 1")
//                .surname("Users 1")
//                .build();
//
//        User updatedUser = User.builder()
//                .name(userCreateDto.getName())
//                .surname(userCreateDto.getSurname())
//                .build();
//
//        User editedUser = User.builder()
//                .id(userId)
//                .name(updatedUser.getName())
//                .surname(updatedUser.getSurname())
//                .build();
//
//        UserDetailsDto editedUserDetailsDto = UserDetailsDto.builder()
//                .id(editedUser.getId())
//                .name(editedUser.getName())
//                .surname(editedUser.getSurname())
//                .build();
//
//        when(userMapperMock.toUser(any(UserCreateDto.class))).thenReturn(updatedUser);
//        when(userServiceMock.edit(eq(userId), eq(updatedUser))).thenReturn(editedUser);
//        when(userMapperMock.toUserDetailsDto(eq(editedUser))).thenReturn(editedUserDetailsDto);
//
//        // when
//        mockMvc.perform(put("/api/v1/user/{id}", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userCreateDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(userId))
//                .andExpect(jsonPath("$.name").value(editedUserDetailsDto.getName()))
//                .andExpect(jsonPath("$.surname").value(editedUserDetailsDto.getSurname()))
//                .andDo(result -> {
//                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
//                    UserDetailsDto actualUserDetailsDto = objectMapper.readValue(response,
//                            UserDetailsDto.class);
//
//                    SoftAssertions softly = new SoftAssertions();
//                    softly.assertThat(actualUserDetailsDto).isNotNull();
//                    softly.assertThat(actualUserDetailsDto.getId()).isEqualTo(editedUserDetailsDto.getId());
//                    softly.assertThat(actualUserDetailsDto.getName()).isEqualTo(editedUserDetailsDto.getName());
//                    softly.assertThat(actualUserDetailsDto.getSurname()).isEqualTo(editedUserDetailsDto.getSurname());
//                    softly.assertAll();
//                });
//    }
//
//    @Test
//    public void testEditNonExistingUser() throws Exception {
//        // given
//        Long userId = 1L;
//
//
//        UserCreateDto userCreateDto = UserCreateDto.builder()
//                .name("User 1")
//                .surname("Users 1")
//                .build();
//
//        User updatedUser = User.builder()
//                .name(userCreateDto.getName())
//                .surname(userCreateDto.getSurname())
//                .build();
//
//        Locale locale = new Locale("ru");
//        String message = messageSource.getMessage(
//                USER_NOT_FOUND_EXCEPTION_MESSAGE,
//                new Object[]{userId},
//                locale
//        );
//
//        when(userMapperMock.toUser(any(UserCreateDto.class))).thenReturn(updatedUser);
//        doThrow(new ResourceNotFoundException(message)).when(userServiceMock).edit(eq(userId),
//                eq(updatedUser));
//
//        ErrorResponse expectedResponseBody = new ErrorResponse(404, message);
//
//        // expect
//        mockMvc.perform(put("/api/v1/user/{id}", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userCreateDto)))
//                .andExpect(status().isNotFound())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(expectedResponseBody.getStatus()))
//                .andExpect(jsonPath("$.message").value(expectedResponseBody.getMessage()))
//                .andDo(result -> {
//                    String response = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
//                    ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
//
//                    SoftAssertions softly = new SoftAssertions();
//                    softly.assertThat(errorResponse).isNotNull();
//                    softly.assertThat(errorResponse.getStatus()).isEqualTo(expectedResponseBody.getStatus());
//                    softly.assertThat(errorResponse.getMessage()).isEqualTo(expectedResponseBody.getMessage());
//                    softly.assertAll();
//                });
//    }
}
