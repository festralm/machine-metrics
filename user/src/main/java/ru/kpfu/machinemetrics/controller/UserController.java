package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.dto.UserCreateDto;
import ru.kpfu.machinemetrics.service.UserService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "${app.api.prefix.v1}/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserRepresentation> listAll() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        String id = jwt.getClaim("sub");
        return userService.findAll(id);
    }

    @GetMapping("/{id}")
    public UserRepresentation get(@PathVariable String id) {
        return userService.findById(id);
    }

    @GetMapping("/current")
    public UserRepresentation getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        String id = jwt.getClaim("sub");
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<UserRepresentation> create(@Valid @RequestBody UserCreateDto userCreateDto) {
        var user = userService.create(userCreateDto);
        return ResponseEntity.created(URI.create("/user/" + user.getId())).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public UserRepresentation edit(
            @PathVariable String id,
            @Valid @RequestBody UserCreateDto userCreateDto
    ) {
        return userService.edit(id, userCreateDto);
    }
}
