package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.dto.UserDetailsDto;
import ru.kpfu.machinemetrics.dto.UserItemDto;
import ru.kpfu.machinemetrics.mapper.UserMapper;
import ru.kpfu.machinemetrics.model.User;
import ru.kpfu.machinemetrics.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserItemDto> listAll() {
        List<User> userList = userService.getAllNotDeleted();
        return userMapper.toUserItemDtos(userList);
    }

    @PostMapping
    public ResponseEntity<UserDetailsDto> create(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = userMapper.toUser(userCreateDto);
        User savedUser = userService.save(user);
        UserDetailsDto savedUserDetailsDto = userMapper.toUserDetailsDto(savedUser);
        return ResponseEntity.created(URI.create("/user/" + savedUser.getId())).body(savedUserDetailsDto);
    }

    @GetMapping("/{id}")
    public UserDetailsDto get(@PathVariable Long id) {
        User user = userService.getById(id);
        UserDetailsDto userDetailsDto = userMapper.toUserDetailsDto(user);
        return userDetailsDto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public UserDetailsDto edit(@PathVariable Long id,
                               @Valid @RequestBody UserCreateDto userCreateDto) {
        User updatedUser = userMapper.toUser(userCreateDto);
        User editedUser = userService.edit(id, updatedUser);
        return userMapper.toUserDetailsDto(editedUser);
    }
}
