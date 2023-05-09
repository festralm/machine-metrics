package ru.kpfu.machinemetrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.model.AuthUser;
import ru.kpfu.machinemetrics.service.AuthUserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "${api.prefix.v1}/auth-user")
@RequiredArgsConstructor
public class UserController {

    private final AuthUserService authUserService;

    @GetMapping
    public List<AuthUser> listAll() {
        return authUserService.getAll();
    }

    @PostMapping
    public ResponseEntity<AuthUser> create(@Valid @RequestBody AuthUser user) {
        AuthUser savedUser = authUserService.save(user);
        return ResponseEntity.created(URI.create("/user/" + savedUser.getId())).body(savedUser);
    }

    @GetMapping("/{id}")
    public AuthUser get(@PathVariable Long id) {
        return authUserService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
