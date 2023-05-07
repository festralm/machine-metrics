package ru.kpfu.machinemetrics.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.dto.UserDetailsDto;
import ru.kpfu.machinemetrics.dto.UserItemDto;
import ru.kpfu.machinemetrics.model.User;

import java.util.List;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testToUser() {
        // given
        UserCreateDto dto =
                UserCreateDto.builder().name("User 1").surname("UserS 1").build();

        // when
        User user = userMapper.toUser(dto);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(user.getName()).isEqualTo(dto.getName());
        softly.assertThat(user.getSurname()).isEqualTo(dto.getSurname());
        softly.assertThat(user.isDeleted()).isFalse();
        softly.assertAll();
    }

    @Test
    public void testToUserCreateDto() {
        // given
        User user = User.builder().id(1L).name("User 1").surname("Users 1").build();

        // when
        UserCreateDto dto = userMapper.toUserCreateDto(user);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(dto.getName()).isEqualTo(user.getName());
        softly.assertThat(dto.getSurname()).isEqualTo(user.getSurname());
        softly.assertAll();
    }

    @Test
    public void testToUserItemDtos() {
        // given
        User user1 = User.builder().id(1L).name("Test User 1").surname("Test UserS 1").deleted(false).build();
        User user2 = User.builder().id(2L).name("Test User 2").surname("Test UserS 2").deleted(false).build();
        List<User> users = List.of(user1, user2);

        // when
        List<UserItemDto> dtos = userMapper.toUserItemDtos(users);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(dtos).hasSize(users.size());

        for (int i = 0; i < dtos.size(); i++) {
            UserItemDto dto = dtos.get(i);
            User user = users.get(i);

            softAssertions.assertThat(dto.getId()).isEqualTo(user.getId());
            softAssertions.assertThat(dto.getName()).isEqualTo(user.getName());
        }

        softAssertions.assertAll();
    }

    @Test
    public void testToUserDetailsDto() {
        // given
        User user = User.builder().id(1L).name("Test User 1").surname("Test UserS 1").deleted(false).build();

        // when
        UserDetailsDto dto = userMapper.toUserDetailsDto(user);

        // then
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(dto.getId()).isEqualTo(user.getId());
        softAssertions.assertThat(dto.getName()).isEqualTo(user.getName());
        softAssertions.assertThat(dto.getSurname()).isEqualTo(user.getSurname());


        softAssertions.assertAll();
    }
}
