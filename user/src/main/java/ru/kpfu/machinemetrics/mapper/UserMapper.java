package ru.kpfu.machinemetrics.mapper;

import org.mapstruct.Mapper;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.dto.UserDetailsDto;
import ru.kpfu.machinemetrics.dto.UserItemDto;
import ru.kpfu.machinemetrics.model.User;

import java.util.List;

@Mapper
public interface UserMapper {

    User toUser(UserCreateDto dto);

    UserCreateDto toUserCreateDto(User user);

    UserDetailsDto toUserDetailsDto(User user);

    List<UserItemDto> toUserItemDtos(List<User> users);
}
