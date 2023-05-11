package ru.kpfu.machinemetrics.controller;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kpfu.machinemetrics.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("${app.api.prefix.v1}/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleRepresentation> findAll() {
        return roleService.findAll();
    }
}
